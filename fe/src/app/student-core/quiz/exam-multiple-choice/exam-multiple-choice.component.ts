import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ConfirmationService, MessageService } from 'primeng/api';
import { QuizService } from '../../../services/quiz/quiz.service';
import { Subscription, interval } from 'rxjs';
import { takeWhile } from 'rxjs/operators';

@Component({
  selector: 'app-exam-multiple-choice',
  templateUrl: './exam-multiple-choice.component.html',
  styleUrl: './exam-multiple-choice.component.css',
  providers: [ConfirmationService]
})
export class ExamMultipleChoiceComponent implements OnInit, OnDestroy {
  submissionId: string | null = null;
  questions: any[] = [];
  currentQuestionIndex: number = 0;
  answers: { [key: number]: number[] } = {};
  remainingTime: number = 0; // in seconds
  originalDuration: number = 0; // in minutes
  timerSubscription?: Subscription;
  examStartTime?: Date;
  isSubmitting: boolean = false;
  examInfo: any = null;
  isFullScreen: boolean = false;
  isFinished: boolean = false;
  progress: number = 0;
  loading: boolean = true;
  isReadyForFullScreen: boolean = false;
  fullScreenAttempted: boolean = false;
  fullScreenRecoveryAttempts: number = 0;
  maxFullScreenRecoveryAttempts: number = 5;
  accidentalExit: boolean = false;
  fullScreenExitTime?: Date;
  fullScreenRecoveryTimeout?: any;
  preventAutoSubmit: boolean = false;
  
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
          this.submitExam();
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

      // Set a short timeout before confirming submission - this helps prevent
      // accidental submission when quickly returning to the window
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
    private confirmationService: ConfirmationService
  ) {}

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      this.submissionId = params.get('id');
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

  ngOnDestroy() {
    this.stopTimer();
    this.exitFullScreen();
    document.removeEventListener('click', this.handleFirstInteraction);
    document.removeEventListener('keydown', this.handleFirstInteraction);
    if (this.fullScreenRecoveryTimeout) {
      clearTimeout(this.fullScreenRecoveryTimeout);
    }
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

  loadExamSubmission() {
    if (!this.submissionId) return;

    this.quizService.getExamSubmission(this.submissionId).subscribe({
      next: (response) => {
        if (response.status === 1) {
          this.examInfo = response.data;
          this.loadExamQuestions();
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: response.message || 'Không thể tải bài kiểm tra',
            life: 3000
          });
          this.router.navigate(['/student/home']);
        }
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Lỗi',
          detail: 'Không thể tải bài kiểm tra',
          life: 3000
        });
        this.router.navigate(['/student/home']);
      }
    });
  }

  loadExamQuestions() {
    if (!this.submissionId) return;

    this.loading = true;
    this.quizService.getExamQuestions(this.submissionId).subscribe({
      next: (response) => {
        if (response.status === 1) {
          this.questions = response.data
            .filter((q: any) => q.questionType === 'MULTIPLE_CHOICE')
            .map((q: any) => {
              console.log('Question data:', q); // For debugging - can be removed in production
              return {
                questionId: q.id,
                question: {
                  questionText: q.text,
                  questionType: q.questionType,
                  points: q.points || 0,
                  allowMultipleAnswers: q.allowMultipleAnswers === true,
                  options: q.options || []
                }
              };
            });
          
          this.initializeAnswers();
          this.initializeTimer();
          this.isReadyForFullScreen = true;
          this.tryEnterFullScreen();
          this.updateProgress();
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: response.message || 'Failed to load exam questions',
            life: 3000
          });
          this.router.navigate(['/student/home']);
        }
        this.loading = false;
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load exam questions',
          life: 3000
        });
        this.router.navigate(['/student/home']);
        this.loading = false;
      }
    });
  }

  determineIfMultipleAnswersAllowed(question: any): boolean {
    return question.allowMultipleAnswers === true;
  }

  tryEnterFullScreen() {
    setTimeout(() => {
      if (!this.isFullScreen && !this.fullScreenAttempted) {
        this.requestFullScreen();
        this.fullScreenAttempted = true;
      }
    }, 500);
  }

  initializeAnswers() {
    this.questions.forEach(question => {
      this.answers[question.questionId] = [];
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
      this.submitExam();
      return;
    }

    this.timerSubscription = interval(1000)
      .pipe(takeWhile(() => this.remainingTime > 0))
      .subscribe(() => {
        this.remainingTime--;
        if (this.remainingTime <= 0) {
          this.submitExam();
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

  getProgressPercentage(): number {
    if (!this.originalDuration) return 0;
    const totalSeconds = this.originalDuration * 60;
    const percentRemaining = (this.remainingTime / totalSeconds) * 100;
    return 100 - percentRemaining;
  }

  previousQuestion() {
    if (this.currentQuestionIndex > 0) {
      this.currentQuestionIndex--;
    }
  }

  nextQuestion() {
    if (this.currentQuestionIndex < this.questions.length - 1) {
      this.currentQuestionIndex++;
    }
  }

  goToQuestion(index: number) {
    if (index >= 0 && index < this.questions.length) {
      this.currentQuestionIndex = index;
    }
  }

  toggleOption(questionId: number, optionId: number) {
    const currentQuestion = this.questions.find(q => q.questionId === questionId);
    if (!currentQuestion) return;

    const isMultipleAnswers = currentQuestion.question.allowMultipleAnswers;
    
    if (isMultipleAnswers) {
      const index = this.answers[questionId].indexOf(optionId);
      if (index > -1) {
        this.answers[questionId].splice(index, 1);
      } else {
        this.answers[questionId].push(optionId);
      }
    } else {
      this.answers[questionId] = [optionId];
    }

    this.submitAnswer(questionId);
    this.updateProgress();
  }

  isOptionSelected(questionId: number, optionId: number): boolean {
    return this.answers[questionId] && this.answers[questionId].includes(optionId);
  }

  submitAnswer(questionId: number) {
    if (!this.submissionId) return;

    const submissionDTO = {
      examSubmissionId: parseInt(this.submissionId),
      questionId: questionId,
      selectedOptionIds: this.answers[questionId]
    };

    this.quizService.submitMultipleChoiceAnswer(submissionDTO).subscribe({
      next: (response) => {
        if (response.status !== 1) {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: response.message || 'Failed to save answer',
            life: 3000
          });
        }
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to save answer',
          life: 3000
        });
      }
    });
  }

  updateProgress() {
    const answered = Object.values(this.answers).filter(ans => ans.length > 0).length;
    this.progress = (answered / this.questions.length) * 100;
  }

  confirmSubmission(message: string = 'Bạn có chắc chắn muốn nộp bài không?') {
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
      header: 'Xác Nhận Nộp Bài',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.submitExam();
      }
    });
  }

  confirmReenterFullScreen() {
    if (this.isFinished || this.isFullScreen) return;
    
    this.confirmationService.confirm({
      message: 'Bạn cần ở chế độ toàn màn hình để tiếp tục bài kiểm tra. Bạn có muốn quay lại chế độ toàn màn hình không?',
      header: 'Yêu Cầu Toàn Màn Hình',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.fullScreenRecoveryAttempts = 0;
        this.requestFullScreen();
      },
      reject: () => {
        this.confirmSubmission('Nếu không ở chế độ toàn màn hình, bạn cần nộp bài. Nộp bài ngay?');
      }
    });
  }

  submitExam() {
    if (!this.submissionId || this.isSubmitting) return;

    this.isSubmitting = true;
    this.quizService.submitExam(this.submissionId).subscribe({
      next: (response) => {
        if (response.status === 1) {
          this.isFinished = true;
          this.stopTimer();
          this.exitFullScreen();
          this.messageService.add({
            severity: 'success',
            summary: 'Thành Công',
            detail: 'Nộp bài thành công',
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
            summary: 'Lỗi',
            detail: response.message || 'Không thể nộp bài',
            life: 3000
          });
        }
      },
      error: (error) => {
        this.isSubmitting = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Lỗi',
          detail: 'Không thể nộp bài',
          life: 3000
        });
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
          summary: 'Chế Độ Toàn Màn Hình',
          detail: 'Không thể vào chế độ toàn màn hình. Vui lòng nhấp vào nút bên dưới.',
          life: 5000,
          sticky: true
        });
      } else {
        this.messageService.add({
          severity: 'warn',
          summary: 'Chế Độ Toàn Màn Hình',
          detail: 'Vui lòng nhấp vào nút "Toàn Màn Hình" để tiếp tục bài kiểm tra.',
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

  isQuestionAnswered(questionId: number): boolean {
    return this.answers[questionId] && this.answers[questionId].length > 0;
  }
}
