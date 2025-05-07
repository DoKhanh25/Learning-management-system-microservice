import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService, ConfirmationService } from 'primeng/api';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { QuizService } from '../../../../services/quiz/quiz.service';
import { UserService } from '../../../../services/user-service/user.service';
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-exam-result-management',
  templateUrl: './exam-result-management.component.html',
  styleUrls: ['./exam-result-management.component.css'],
  providers: [MessageService, ConfirmationService]
})
export class ExamResultManagementComponent implements OnInit {
  courseId: number | null = null;
  examId: number | null = null;
  exam: any = null;
  submissions: any[] = [];
  selectedSubmission: any = null;
  submissionDetails: any = null;
  questionSubmissions: any[] = [];
  loading = false;

  // Maps to store user information
  userMap: {[key: string]: any} = {};

  displayGradeDialog = false;
  gradeForm: FormGroup;
  currentQuestionSubmission: any = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private quizService: QuizService,
    private userService: UserService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private fb: FormBuilder
  ) {
    this.gradeForm = this.fb.group({
      score: [0, [Validators.required, Validators.min(0)]],
      feedback: ['']
    });
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.courseId = Number(params.get('courseId'));
      this.examId = Number(params.get('examId'));

      if (this.examId) {
        this.loadExamData();
        this.loadSubmissions();
      }
    });
  }

  loadExamData(): void {
    if (!this.examId) return;

    this.loading = true;
    this.quizService.getExamById(this.examId).subscribe({
      next: (response) => {
        if (response.status === 1) {
          this.exam = response.data;
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to load exam data',
            life: 3000
          });
        }
        this.loading = false;
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load exam data',
          life: 3000
        });
        this.loading = false;
      }
    });
  }

  loadSubmissions(): void {
    if (!this.examId) return;

    this.loading = true;
    this.quizService.getExamSubmissions(this.examId).subscribe({
      next: (response) => {
        if (response.status === 1) {
          this.submissions = response.data || [];
          this.loadUserInformation();
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to load submissions',
            life: 3000
          });
        }
        this.loading = false;
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load submissions',
          life: 3000
        });
        this.loading = false;
      }
    });
  }

  loadUserInformation(): void {
    if (!this.submissions || this.submissions.length === 0) return;

    // Extract unique user IDs from submissions
    const userIds = [...new Set(this.submissions.map(sub => sub.userId))];

    if (userIds.length === 0) return;

    this.userService.getUsersByIds(userIds).subscribe({
      next: (response) => {
        if (response.status === 1) {
          const users = response.data || [];
          // Create a map of user IDs to user objects for easy lookup
          this.userMap = {};
          users.forEach((user:any) => {
            this.userMap[user.userId] = user;
          });
        }
      },
      error: (err) => {
        console.error('Error loading user information:', err);
      }
    });
  }

  getUserName(userId: string): string {
    if (this.userMap[userId]) {
      const user = this.userMap[userId];
      return user.fullname || `${user.firstName || ''} ${user.lastName || ''}`.trim() || userId;
    }
    return userId;
  }

  viewSubmission(submission: any): void {
    this.selectedSubmission = submission;
    this.loading = true;

    // Get submission details and question submissions
    forkJoin({
      details: this.quizService.getExamSubmission(submission.id),
      questions: this.quizService.getQuestionSubmissions(submission.id)
    }).subscribe({
      next: (results) => {
        if (results.details.status === 1) {
          this.submissionDetails = results.details.data;
        }

        if (results.questions.status === 1) {
          this.questionSubmissions = results.questions.data || [];
          // Sort submissions by question ID
          this.questionSubmissions.sort((a, b) => a.questionId - b.questionId);
        }

        this.loading = false;
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load submission details',
          life: 3000
        });
        this.loading = false;
      }
    });
  }

  getQuestionTypeLabel(questionSubmission: any): string {
    switch(this.getQuestionType(questionSubmission)) {
      case 'MULTIPLE_CHOICE':
        return 'Trắc nghiệm';
      case 'ESSAY':
        return 'Tự luận';
      case 'CODING':
        return 'Lập trình';
      default:
        return 'Không xác định';
    }
  }

  getQuestionType(questionSubmission: any): string {
    if (questionSubmission.selectedOptionIds !== undefined) {
      return 'MULTIPLE_CHOICE';
    } else if (questionSubmission.answerText !== undefined) {
      return 'ESSAY';
    } else if (questionSubmission.submittedCode !== undefined) {
      return 'CODING';
    }
    return 'UNKNOWN';
  }

  needsManualGrading(questionSubmission: any): boolean {
    const type = this.getQuestionType(questionSubmission);
    return type === 'ESSAY' || type === 'CODING';
  }

  openGradeDialog(questionSubmission: any): void {
    this.currentQuestionSubmission = questionSubmission;
    this.gradeForm.patchValue({
      score: questionSubmission.score || 0,
      feedback: questionSubmission.feedback || ''
    });
    this.displayGradeDialog = true;
  }

  submitGrade(): void {
    if (this.gradeForm.invalid || !this.currentQuestionSubmission) {
      return;
    }

    const formValue = this.gradeForm.value;
    const questionType = this.getQuestionType(this.currentQuestionSubmission);
    const data = {
      submissionId: this.selectedSubmission.id,
      questionSubmissionId: this.currentQuestionSubmission.id,
      score: formValue.score,
      feedback: formValue.feedback
    };

    let gradeMethod;
    if (questionType === 'ESSAY') {
      gradeMethod = this.quizService.updateEssaySubmissionGrade(data);
    } else if (questionType === 'CODING') {
      gradeMethod = this.quizService.updateCodingSubmissionGrade(data);
    } else {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Cannot grade this type of question',
        life: 3000
      });
      return;
    }

    gradeMethod.subscribe({
      next: (response) => {
        if (response.status === 1) {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Grade updated successfully',
            life: 3000
          });
          this.displayGradeDialog = false;
          // Refresh submission details
          this.viewSubmission(this.selectedSubmission);
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: response.message || 'Failed to update grade',
            life: 3000
          });
        }
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to update grade',
          life: 3000
        });
      }
    });
  }

  finalizeGrading(): void {
    if (!this.selectedSubmission) return;

    this.confirmationService.confirm({
      message: 'Are you sure you want to finalize grading for this submission? This action cannot be undone.',
      accept: () => {
        this.quizService.finalizeGrading(this.selectedSubmission.id).subscribe({
          next: (response) => {
            if (response.status === 1) {
              this.messageService.add({
                severity: 'success',
                summary: 'Success',
                detail: 'Grading finalized successfully',
                life: 3000
              });
              // Refresh submission details and list
              this.loadSubmissions();
              this.viewSubmission(this.selectedSubmission);
            } else {
              this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: response.message || 'Failed to finalize grading',
                life: 3000
              });
            }
          },
          error: (error) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Failed to finalize grading',
              life: 3000
            });
          }
        });
      }
    });
  }

  backToCourse(): void {
    if (this.courseId) {
      this.router.navigate(['/user/course-detail', this.courseId]);
    }
  }

  backToExams(): void {
    if (this.courseId) {
      this.router.navigate(['/user/course', this.courseId, 'exam-management']);
    }
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleString();
  }

  formatDateVN(date: string): string {
    if (!date) return '';
    const d = new Date(date);
    return `${d.getDate()}/${d.getMonth() + 1}/${d.getFullYear()} ${d.getHours()}:${d.getMinutes().toString().padStart(2, '0')}`;
  }

  getStatusSeverity(submission: any): any {
    if (submission.isGraded) {
      return 'success';
    } else if (submission.submissionTime) {
      return 'warning';
    } else {
      return 'info';
    }
  }

  getStatus(submission: any): string {
    if (submission.isGraded) {
      return 'Đã chấm';
    } else if (submission.submissionTime) {
      return 'Đã nộp';
    } else {
      return 'Đang làm';
    }
  }

  getExamStatusTranslation(exam: any): string {
    if (new Date(exam.startTime) > new Date()) {
      return 'Chưa bắt đầu';
    } else if (new Date(exam.endTime) < new Date()) {
      return 'Đã kết thúc';
    } else {
      return 'Đang diễn ra';
    }
  }
}
