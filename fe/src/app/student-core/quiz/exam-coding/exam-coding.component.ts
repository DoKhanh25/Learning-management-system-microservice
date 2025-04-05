import { Component, OnInit, ViewChild, ElementRef, OnDestroy, HostListener, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { QuizService } from '../../../services/quiz/quiz.service';
import { interval, Subscription } from 'rxjs';
import { CodingQuestion } from '../../../../model/quiz';
import { editor } from 'monaco-editor';
import IStandaloneCodeEditor = editor.IStandaloneCodeEditor;
import { ConfirmationService, MessageService } from 'primeng/api';
import { takeWhile, take } from 'rxjs/operators';

@Component({
  selector: 'app-exam-coding',
  templateUrl: './exam-coding.component.html',
  styleUrl: './exam-coding.component.css',
  providers: [ConfirmationService, MessageService],
  changeDetection: ChangeDetectionStrategy.OnPush // Add OnPush change detection
})
export class ExamCodingComponent implements OnInit, OnDestroy {
  submissionId: string | null = null;
  questionId!: number;
  questions!: any[];
  examInfo: any = null;

  editor!: IStandaloneCodeEditor;
  editorOptions = {
    theme: 'vs-dark',
    language: 'javascript',
    fontSize: 14,
    automaticLayout: true,
    height: '500px'
  };

  code: string = '// Write your code here';
  executionResult: any = null;
  isExecuting: boolean = false;
  isSubmitting: boolean = false;
  loading: boolean = true;

  // For timer
  remainingTime: number = 0; // in seconds
  originalDuration: number = 0; // in minutes
  timerSubscription!: Subscription;
  examStartTime?: Date;
  examEndTime?: Date;
  timeRemaining: string = '00:00:00'; // Add variable to store formatted time

  selectedLanguage: string = 'javascript';

  // Fullscreen functionality
  isFullScreen: boolean = false;
  isFinished: boolean = false;
  isReadyForFullScreen: boolean = false;
  fullScreenAttempted: boolean = false;
  fullScreenRecoveryAttempts: number = 0;
  maxFullScreenRecoveryAttempts: number = 5;
  accidentalExit: boolean = false;
  fullScreenExitTime?: Date;
  fullScreenRecoveryTimeout?: any;
  preventAutoSubmit: boolean = false;

  editorInitialized: boolean = false;
  editorDisposed: boolean = false;

  // Add this property for toggling raw output display
  showOutputDetails: boolean = false;

  @HostListener('document:fullscreenchange', ['$event'])
  @HostListener('document:webkitfullscreenchange', ['$event'])
  @HostListener('document:mozfullscreenchange', ['$event'])
  @HostListener('document:MSFullscreenChange', ['$event'])
  fullScreenChange() {
    const wasFullScreen = this.isFullScreen;
    this.isFullScreen = !!document.fullscreenElement;

    // Handle exit from full screen
    if (wasFullScreen && !this.isFullScreen && !this.isFinished) {
      this.fullScreenExitTime = new Date();

      // Show a message to the user
      this.messageService.add({
        severity: 'warn',
        summary: 'Thoát Chế Độ Toàn Màn Hình',
        detail: 'Bạn đã thoát khỏi chế độ toàn màn hình. Bài thi sẽ được tự động nộp.',
        life: 3000
      });

      // Wait a very short time then submit the exam
      setTimeout(() => {
        if (!this.isFinished && !this.isFullScreen) {
          this.submitAnswer();
        }
      }, 1500);
    } else if (!wasFullScreen && this.isFullScreen) {
      // Successfully entered fullscreen mode
      this.messageService.add({
        severity: 'success',
        summary: 'Chế Độ Toàn Màn Hình',
        detail: 'Bạn đang ở chế độ toàn màn hình',
        life: 2000
      });
    }
  }

  @HostListener('window:beforeunload', ['$event'])
  unloadNotification($event: any) {
    if (!this.isFinished) {
      $event.returnValue = true;
    }
  }

  @HostListener('window:blur', ['$event'])
  onWindowBlur(event: any) {
    if (!this.isFinished && !this.preventAutoSubmit) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Cảnh Báo',
        detail: 'Không được phép chuyển tab hoặc cửa sổ trong khi làm bài',
        life: 3000
      });
      setTimeout(() => {
        if (!document.hasFocus() && !this.isFinished) {
          this.confirmSubmission('Chuyển tab sẽ tự động nộp bài. Tiếp tục?');
        }
      }, 800);
    }
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private quizService: QuizService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private cdr: ChangeDetectorRef // Add change detector ref
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.submissionId = params['id'];
      if (this.submissionId) {
        this.loadExamSubmission();
      } else {
        this.router.navigate(['/student/home']);
      }
    });

    // Listen for first user interaction to trigger full screen
    document.addEventListener('click', this.handleFirstInteraction);
    document.addEventListener('keydown', this.handleFirstInteraction);
  }

  ngOnDestroy(): void {
    this.stopTimer();
    this.exitFullScreen();
    document.removeEventListener('click', this.handleFirstInteraction);
    document.removeEventListener('keydown', this.handleFirstInteraction);
    if (this.fullScreenRecoveryTimeout) {
      clearTimeout(this.fullScreenRecoveryTimeout);
    }

    // Properly dispose of editor
    this.disposeEditor();
  }

  handleFirstInteraction = () => {
    if (this.isReadyForFullScreen && !this.fullScreenAttempted && !this.isFullScreen) {
      this.requestFullScreen();
      this.fullScreenAttempted = true;

      // Remove the event listeners once we've attempted full screen
      document.removeEventListener('click', this.handleFirstInteraction);
      document.removeEventListener('keydown', this.handleFirstInteraction);
    }
  }

  disposeEditor(): void {
    if (this.editor && !this.editorDisposed) {
      this.editor.dispose();
      this.editorDisposed = true;
    }
  }

  loadExamSubmission() {
    if (!this.submissionId) return;

    this.loading = true;
    this.quizService.getExamSubmission(this.submissionId).subscribe({
      next: (response) => {
        if (response.status === 1) {
          this.examInfo = response.data;
          this.loadQuestion();
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: response.message || 'Failed to load exam',
            life: 3000
          });
          // this.router.navigate(['/student/home']);
        }
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load exam',
          life: 3000
        });
        this.router.navigate(['/student/home']);
        this.loading = false;
      }
    });
  }

  onEditorInit(editor: any): void {
    this.editor = editor;
    this.editorInitialized = true;

    if (this.selectedLanguage) {
      this.updateEditorLanguage(this.selectedLanguage);
    }

    // Reduced model update frequency
    if (this.editor) {
      this.editor.getModel()?.onDidChangeContent((e) => {
        // Debounce model updates
        if (this._modelUpdateTimeout) {
          clearTimeout(this._modelUpdateTimeout);
        }

        this._modelUpdateTimeout = setTimeout(() => {
          this.code = this.editor.getValue();
          this.cdr.markForCheck();
        }, 300);
      });
    }
  }

  private _modelUpdateTimeout: any;

  loadQuestion(): void {
    if (!this.submissionId) return;

    this.loading = true;
    this.cdr.markForCheck();

    this.quizService.getExamQuestions(this.submissionId).subscribe({
      next: (response) => {
        if (response.status === 1) {
          // Filter and map coding questions
          this.questions = response.data
            .filter((q: any) => q.questionType === 'CODING')
            .map((q: any) => {
              return {
                questionId: q.id,
                question: {
                  questionText: q.text,
                  questionType: q.questionType,
                  points: q.points || 0,
                  starterCode: q.starterCode || '// Write your code here',
                  programmingLanguage: q.programmingLanguage || 'javascript'
                }
              };
            });

          // Set the current question and update editor
          if (this.questions && this.questions.length > 0) {
            const currentQuestion = this.questions[0];
            this.questionId = currentQuestion.questionId;

            // Set default language from question
            if (currentQuestion.question.programmingLanguage) {
              this.selectedLanguage = currentQuestion.question.programmingLanguage;
              this.updateEditorLanguage(this.selectedLanguage);
            }

            // Set starter code if available
            if (currentQuestion.question.starterCode) {
              this.code = currentQuestion.question.starterCode;

              // Only update the editor value if it's already initialized
              setTimeout(() => {
                if (this.editor && this.editorInitialized) {
                  this.editor.setValue(this.code);
                }
              }, 100);
            }
          }

          // Initialize timer
          this.initializeTimer();
          this.isReadyForFullScreen = true;
          this.tryEnterFullScreen();

          this.loading = false;
          this.cdr.markForCheck();
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Question not found or not a coding question',
            life: 3000
          });
          this.loading = false;
          this.cdr.markForCheck();
        }
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load question',
          life: 3000
        });
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  tryEnterFullScreen() {
    setTimeout(() => {
      if (!this.isFullScreen && !this.fullScreenAttempted) {
        this.requestFullScreen();
        this.fullScreenAttempted = true;
      }
    }, 500);
  }

  updateEditorLanguage(language: string): void {
    // Map language names to Monaco editor language identifiers
    let editorLang = language.toLowerCase();

    // Handle special cases
    if (editorLang === 'c++') editorLang = 'cpp';
    if (editorLang === 'c#') editorLang = 'csharp';
    if(editorLang === 'java') editorLang = 'java';
    if (editorLang === 'python') editorLang = 'python';
    if (editorLang === 'javascript') editorLang = 'javascript';
    if (editorLang === 'golang') editorLang = 'go';

    // Only update if editor is initialized
    if (this.editorInitialized && this.editor) {
      const model = this.editor.getModel();
      if (model) {
        editor.setModelLanguage(model, editorLang);
      }
    } else {
      this.editorOptions = {
        ...this.editorOptions,
        language: editorLang
      };
    }
  }

  executeCode(): void {
    if (!this.questionId) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'No question selected',
        life: 3000
      });
      return;
    }

    this.isExecuting = true;
    this.executionResult = null;
    this.showOutputDetails = false; // Reset output detail toggle

    const codeToExecute = this.editor ? this.editor.getValue() : this.code;

    const executionRequest = {
      examSubmissionId: parseInt(this.submissionId!),
      questionId: this.questionId,
      language: this.selectedLanguage,
      stdin: codeToExecute
    };

    this.quizService.executeCode(executionRequest).subscribe({
      next: (response) => {
        this.isExecuting = false;
        if (response.status === 1) {
          this.executionResult = response.data;
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: response.message || 'Execution failed',
            life: 3000
          });
          this.executionResult = {
            error: response.message,
            output: 'Execution failed',
            testsPassed: false
          };
        }
        this.cdr.markForCheck();
      },
      error: (error) => {
        this.isExecuting = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Server error during execution',
          life: 3000
        });
        this.executionResult = {
          error: 'Server error during execution',
          output: 'Could not execute code due to server error',
          testsPassed: false
        };
        this.cdr.markForCheck();
      }
    });
  }

  // Add method to toggle output details display
  toggleOutputDetails(): void {
    this.showOutputDetails = !this.showOutputDetails;
    this.cdr.markForCheck();
  }

  submitAnswer(): void {
    if (this.isSubmitting || !this.questionId) return;

    this.isSubmitting = true;

    const codeToSubmit = this.editor ? this.editor.getValue() : this.code;

    const submission = {
      examSubmissionId: parseInt(this.submissionId!),
      questionId: this.questionId,
      submittedCode: codeToSubmit,
      language: this.selectedLanguage,
      testResults: this.executionResult ? JSON.stringify(this.executionResult) : null,
      compilationOutput: this.executionResult ? this.executionResult.output : null,
      executionTime: null
    };

    this.quizService.submitCodingAnswer(submission).subscribe({
      next: (response) => {
        if (response.status === 1) {
          this.isFinished = true;
          this.stopTimer();
          this.exitFullScreen();
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Answer submitted successfully',
            life: 3000
          });

          // Navigate back to course detail after a short delay
          setTimeout(() => {
            if (this.examInfo && this.examInfo.exam) {
              this.router.navigate(['/student/course-detail', this.examInfo.exam.courseId]);
            } else {
              this.router.navigate(['/student/home']);
            }
          }, 3000);
        } else {
          this.isSubmitting = false;
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: response.message || 'Failed to submit answer',
            life: 3000
          });
        }
        this.cdr.markForCheck();
      },
      error: (error) => {
        this.isSubmitting = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to submit answer',
          life: 3000
        });
        this.cdr.markForCheck();
      }
    });
  }

  initializeTimer() {
    if (!this.examInfo) return;

    this.examStartTime = new Date(this.examInfo.startTime);
    const examDuration = this.examInfo.exam?.duration || 60;
    this.originalDuration = examDuration;
    const elapsedTime = (new Date().getTime() - this.examStartTime.getTime()) / 1000;
    this.remainingTime = (examDuration * 60) - elapsedTime;

    if (this.remainingTime <= 0) {
      this.submitAnswer();
      return;
    }

    // Optimize timer updates: only update UI every second instead of on every tick
    this.timerSubscription = interval(1000)
      .pipe(takeWhile(() => this.remainingTime > 0))
      .subscribe(() => {
        this.remainingTime--;
        this.timeRemaining = this.formatTime(this.remainingTime);
        this.cdr.markForCheck();

        if (this.remainingTime <= 0) {
          this.submitAnswer();
        }
      });
  }

  stopTimer() {
    if (this.timerSubscription) {
      this.timerSubscription.unsubscribe();
    }
  }

  formatTime(seconds: number): string {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = Math.floor(seconds % 60);

    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }

  confirmSubmission(message: string = 'Are you sure you want to submit your answer?') {
    if (this.accidentalExit && this.fullScreenExitTime) {
      const timeSinceExit = new Date().getTime() - this.fullScreenExitTime.getTime();
      if (timeSinceExit < 2000) {
        return;
      }
    }

    if (this.preventAutoSubmit) {
      this.confirmReenterFullScreen();
      return;
    }

    this.confirmationService.confirm({
      message: message,
      header: 'Confirm Submission',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.submitAnswer();
      }
    });
  }

  confirmReenterFullScreen() {
    if (this.isFinished || this.isFullScreen) return;

    this.confirmationService.confirm({
      message: 'You need to be in fullscreen mode to continue the exam. Do you want to return to fullscreen mode?',
      header: 'Fullscreen Required',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.fullScreenRecoveryAttempts = 0;
        this.requestFullScreen();
      },
      reject: () => {
        this.confirmSubmission('If not in fullscreen mode, you need to submit your answer. Submit now?');
      }
    });
  }

  requestFullScreen() {
    const docEl = document.documentElement;

    try {
      if (docEl.requestFullscreen) {
        docEl.requestFullscreen();
      } else if ((docEl as any).mozRequestFullScreen) {
        (docEl as any).mozRequestFullScreen();
      } else if ((docEl as any).webkitRequestFullscreen) {
        (docEl as any).webkitRequestFullscreen();
      } else if ((docEl as any).msRequestFullscreen) {
        (docEl as any).msRequestFullscreen();
      }
    } catch (error) {
      console.error('Failed to enter fullscreen:', error);
      this.fullScreenRecoveryAttempts++;

      // If we failed too many times, show a persistent message
      if (this.fullScreenRecoveryAttempts >= this.maxFullScreenRecoveryAttempts) {
        this.messageService.add({
          severity: 'error',
          summary: 'Fullscreen Mode',
          detail: 'Unable to enter fullscreen mode. Please click the button below.',
          life: 5000,
          sticky: true
        });
      } else {
        this.messageService.add({
          severity: 'warn',
          summary: 'Fullscreen Mode',
          detail: 'Please click the "Fullscreen" button to continue the exam.',
          life: 5000
        });
      }
    }
  }

  exitFullScreen() {
    if (document.exitFullscreen) {
      document.exitFullscreen();
    } else if ((document as any).mozCancelFullScreen) {
      (document as any).mozCancelFullScreen();
    } else if ((document as any).webkitExitFullscreen) {
      (document as any).webkitExitFullscreen();
    } else if ((document as any).msExitFullscreen) {
      (document as any).msExitFullscreen();
    }
  }
}
