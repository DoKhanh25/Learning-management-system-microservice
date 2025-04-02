import { Component, OnInit } from '@angular/core';
import { QuizService } from '../../../../services/quiz/quiz.service';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService, ConfirmationService } from 'primeng/api';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-question-bank-management',
  templateUrl: './question-bank-management.component.html',
  styleUrls: ['./question-bank-management.component.css'],
  providers: [MessageService, ConfirmationService]
})
export class QuestionBankManagementComponent implements OnInit {
  courseId!: number;
  questionBanks: any[] = [];
  loading: boolean = true;
  displayDialog: boolean = false;
  questionBankForm: FormGroup;
  isEditMode: boolean = false;
  editingId: number | null = null;
  searchQuery!: string;

  questionTypes = [
    { label: 'Trắc nghiệm', value: 'MULTIPLE_CHOICE' },
    { label: 'Tự luận', value: 'ESSAY' },
    { label: 'Lập trình', value: 'CODING' }
  ];

  constructor(
    private quizService: QuizService,
    private route: ActivatedRoute,
    private router: Router,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private fb: FormBuilder
  ) {
    this.questionBankForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      questionType: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.courseId = params['courseId'];
      this.loadQuestionBanks();
    });
  }

  loadQuestionBanks(): void {
    this.loading = true;
    this.quizService.getAllQuestionBanksByCourseId(this.courseId).subscribe({
      next: (result) => {
        if (result.status === 1) {
          this.questionBanks = result.data;
        } else {
          this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải ngân hàng câu hỏi' });
        }
        this.loading = false;
      },
      error: (error) => {
        this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải ngân hàng câu hỏi' });
        this.loading = false;
      }
    });
  }

  openNew(): void {
    this.isEditMode = false;
    this.editingId = null;
    this.questionBankForm.reset();
    this.displayDialog = true;
  }

  editQuestionBank(questionBank: any): void {
    this.isEditMode = true;
    this.editingId = questionBank.id;
    this.questionBankForm.patchValue({
      name: questionBank.name,
      description: questionBank.description,
      questionType: questionBank.questionType
    });
    this.displayDialog = true;
  }

  deleteQuestionBank(questionBank: any): void {
    this.confirmationService.confirm({
      message: 'Bạn có chắc chắn muốn xóa ngân hàng câu hỏi này?',
      header: 'Xác nhận xóa',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.quizService.deleteQuestionBank(questionBank.id).subscribe({
          next: (result) => {
            if (result.status === 1) {
              this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa ngân hàng câu hỏi thành công' });
              this.loadQuestionBanks();
            } else {
              this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể xóa ngân hàng câu hỏi' });
            }
          },
          error: (error) => {
            this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể xóa ngân hàng câu hỏi' });
          }
        });
      }
    });
  }

  saveQuestionBank(): void {
    if (this.questionBankForm.invalid) {
      this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Vui lòng điền đầy đủ các trường bắt buộc' });
      return;
    }

    const questionBankDTO = {
      ...this.questionBankForm.value,
      courseId: this.courseId
    };

    if (this.isEditMode && this.editingId) {
      questionBankDTO.id = this.editingId;
    }

    this.quizService.createQuestionBank(questionBankDTO).subscribe({
      next: (result) => {
        if (result.status === 1) {
          this.messageService.add({ 
            severity: 'success', 
            summary: 'Thành công', 
            detail: this.isEditMode ? 'Cập nhật ngân hàng câu hỏi thành công' : 'Tạo ngân hàng câu hỏi thành công' 
          });
          this.displayDialog = false;
          this.loadQuestionBanks();
        } else {
          this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể lưu ngân hàng câu hỏi' });
        }
      },
      error: (error) => {
        this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể lưu ngân hàng câu hỏi' });
      }
    });
  }

  hideDialog(): void {
    this.displayDialog = false;
  }

  getQuestionTypeName(type: string): string {
    const found = this.questionTypes.find(t => t.value === type);
    return found ? found.label : type;
  }

  navigateToQuestionBankDetail(questionBank: any): void {
    this.router.navigate(['/user/course', this.courseId, 'question-bank-detail', questionBank.id]);
  }
}
