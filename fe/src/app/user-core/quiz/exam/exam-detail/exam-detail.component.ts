import { Component, OnInit } from '@angular/core';
import { QuizService } from '../../../../services/quiz/quiz.service';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService, ConfirmationService } from 'primeng/api';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Exam, ExamQuestion, QuestionBank } from '../../../../../model/quiz';
import { log } from 'console';

@Component({
  selector: 'app-exam-detail',
  templateUrl: './exam-detail.component.html',
  styleUrls: ['./exam-detail.component.css'],
  providers: [MessageService, ConfirmationService]
})
export class ExamDetailComponent implements OnInit {
  courseId!: number;
  examId!: number;
  exam!: Exam;
  questionBanks: QuestionBank[] = [];
  loading: boolean = true;
  questionsLoading: boolean = false;
  selectedQuestionBanks: QuestionBank[] = [];
  addQuestionsForm: FormGroup;
  singleBankDialogVisible: boolean = false;
  multipleBanksDialogVisible: boolean = false;

  // For multiple bank selection
  multipleSelectionMode: boolean = false;
  selectedQuestionBankIds: number[] = [];

  constructor(
    private quizService: QuizService,
    private route: ActivatedRoute,
    private router: Router,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private fb: FormBuilder
  ) {
    this.addQuestionsForm = this.fb.group({
      questionBankId: [null, Validators.required],
      numberOfQuestions: [1, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.courseId = params['courseId'];
      this.examId = params['examId'];
      this.loadExamDetails();
    });
  }

  loadExamDetails(): void {
    this.loading = true;
    this.quizService.getExamById(this.examId).subscribe({
      next: (result) => {
        if (result.status === 1) {
          this.exam = result.data;
          // Load question banks after exam details are loaded
          this.loadQuestionBanks();

          // Load exam questions using findQuestionByExamId
          this.loadExamQuestions();
        } else {
          this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải thông tin bài kiểm tra' });
        }
        this.loading = false;
      },
      error: (error) => {
        this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải thông tin bài kiểm tra' });
        this.loading = false;
      }
    });
  }

  loadExamQuestions(): void {
    this.questionsLoading = true;
    this.quizService.findQuestionByExamId(this.examId).subscribe({
      next: (result) => {
        if (result.status === 1) {
          this.exam.examQuestions = result.data;
          console.log(this.exam.examQuestions);
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Không thể tải danh sách câu hỏi của bài kiểm tra'
          });
        }
        this.questionsLoading = false;
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Lỗi',
          detail: 'Không thể tải danh sách câu hỏi của bài kiểm tra'
        });
        this.questionsLoading = false;
      }
    });
  }

  loadQuestionBanks(): void {
    this.quizService.getAllQuestionBanksByCourseId(this.courseId).subscribe({
      next: (result) => {
        if (result.status === 1) {
          this.questionBanks = result.data;

          this.selectedQuestionBanks = this.questionBanks.filter(bank =>
            bank.questionType === this.exam?.examType);
        } else {
          this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải ngân hàng câu hỏi' });
        }
      },
      error: (error) => {
        this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải ngân hàng câu hỏi' });
      }
    });
  }

  openAddQuestionsDialog(): void {
    // Recreate the form with all required controls instead of just resetting it
    this.addQuestionsForm = this.fb.group({
      questionBankId: [null, Validators.required],
      numberOfQuestions: [1, [Validators.required, Validators.min(1)]]
    });
    this.multipleSelectionMode = false;
    this.selectedQuestionBankIds = [];
    this.singleBankDialogVisible = true;
  }

  openMultipleBanksDialog(): void {
    this.addQuestionsForm = this.fb.group({
      numberOfQuestions: [1, [Validators.required, Validators.min(1)]]
    });
    this.multipleSelectionMode = true;
    this.selectedQuestionBankIds = [];
    this.multipleBanksDialogVisible = true;
  }

  // Add this new method to handle question bank selection
  onQuestionBankSelectionChange(event: any): void {
    // Update selectedQuestionBankIds based on the selection event
    this.selectedQuestionBankIds = event.value || [];
    console.log('Selected question bank IDs:', this.selectedQuestionBankIds);
  }

  onSingleQuestionBankChange(event: any): void {
    console.log('Selected single question bank:', event.value);
    this.addQuestionsForm.patchValue({
      questionBankId: event.value
    });
    console.log('Selected single question bank ID:', event.value);
  }

  addQuestionsFromBank(): void {
    if (this.addQuestionsForm.invalid) {
      Object.keys(this.addQuestionsForm.controls).forEach(key => {
        this.addQuestionsForm.get(key)?.markAsTouched();
      });
      this.messageService.add({
        severity: 'error',
        summary: 'Lỗi',
        detail: 'Vui lòng điền đầy đủ các trường bắt buộc'
      });
      return;
    }

    // Additional validation for multiple selection mode
    if (this.multipleSelectionMode && this.selectedQuestionBankIds.length === 0) {
      this.messageService.add({
        severity: 'error',
        summary: 'Lỗi',
        detail: 'Vui lòng chọn ít nhất một ngân hàng câu hỏi'
      });
      return;
    }

    this.questionsLoading = true;

    if (this.multipleSelectionMode) {
      const numberOfQuestions = this.addQuestionsForm.get('numberOfQuestions')?.value;

      this.quizService.addQuestionsFromMultipleQuestionBanks(
        this.examId,
        this.selectedQuestionBankIds,
        numberOfQuestions
      ).subscribe({
        next: (result) => {
          if (result.status === 1) {
            this.messageService.add({
              severity: 'success',
              summary: 'Thành công',
              detail: `Đã thêm ${numberOfQuestions} câu hỏi vào bài kiểm tra`
            });
            this.multipleBanksDialogVisible = false;
            this.loadExamDetails();
          } else {
            this.messageService.add({
              severity: 'error',
              summary: 'Lỗi',
              detail: result.message || 'Không thể thêm câu hỏi vào bài kiểm tra'
            });
          }
          this.questionsLoading = false;
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Không thể thêm câu hỏi vào bài kiểm tra'
          });
          this.questionsLoading = false;
        }
      });
    } else {
      const questionBankId = this.addQuestionsForm.get('questionBankId')?.value;
      const numberOfQuestions = this.addQuestionsForm.get('numberOfQuestions')?.value;
      
      // Add explicit validation for questionBankId
      if (!questionBankId) {
        this.messageService.add({
          severity: 'error',
          summary: 'Lỗi',
          detail: 'Vui lòng chọn ngân hàng câu hỏi'
        });
        this.questionsLoading = false;
        return;
      }

      this.quizService.addQuestionsFromQuestionBank(
        this.examId,
        questionBankId,
        numberOfQuestions
      ).subscribe({
        next: (result) => {
          if (result.status === 1) {
            this.messageService.add({
              severity: 'success',
              summary: 'Thành công',
              detail: `Đã thêm ${numberOfQuestions} câu hỏi vào bài kiểm tra`
            });
            this.singleBankDialogVisible = false;
            this.loadExamDetails();
          } else {
            this.messageService.add({
              severity: 'error',
              summary: 'Lỗi',
              detail: result.message || 'Không thể thêm câu hỏi vào bài kiểm tra'
            });
          }
          this.questionsLoading = false;
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Không thể thêm câu hỏi vào bài kiểm tra'
          });
          this.questionsLoading = false;
        }
      });
    }
  }

  removeQuestion(question: any): void {
    this.confirmationService.confirm({
      message: 'Bạn có chắc chắn muốn xóa câu hỏi này khỏi bài kiểm tra?',
      header: 'Xác nhận xóa',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.quizService.removeQuestionFromExam(this.examId, question.id).subscribe({
          next: (result) => {
            if (result.status === 1) {
              this.messageService.add({
                severity: 'success',
                summary: 'Thành công',
                detail: 'Đã xóa câu hỏi khỏi bài kiểm tra'
              });
              this.loadExamDetails();
            } else {
              this.messageService.add({
                severity: 'error',
                summary: 'Lỗi',
                detail: 'Không thể xóa câu hỏi khỏi bài kiểm tra'
              });
            }
          },
          error: (error) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Lỗi',
              detail: 'Không thể xóa câu hỏi khỏi bài kiểm tra'
            });
          }
        });
      }
    });
  }

  getQuestionBankNameById(id: number): any {
    const bank = this.questionBanks.find(bank => bank.id === id);
    return bank ? bank.name : 'Không xác định';
  }

  goBackToExams(): void {
    this.router.navigate(['/user/course', this.courseId, 'exam-management']);
  }

  getExamTypeName(type: string): string {
    const examTypes = [
      { label: 'Trắc nghiệm', value: 'MULTIPLE_CHOICE' },
      { label: 'Tự luận', value: 'ESSAY' },
      { label: 'Lập trình', value: 'CODING' }
    ];
    const found = examTypes.find(t => t.value === type);
    return found ? found.label : type;
  }

  formatDateTime(date: string | Date | undefined): string {
    if (!date) return 'Chưa thiết lập';
    return new Date(date).toLocaleString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getQuestionContent(question: any): string {
    // Safely get content
    if (!question.text) {
      return 'Nội dung câu hỏi không có sẵn';
    }
    // Remove HTML tags from the content
    const content = question.text.replace(/(<([^>]+)>)/gi, '');
    // Truncate long content
    return content.length > 100 ? content.substring(0, 100) + '...' : content;
  }
}
