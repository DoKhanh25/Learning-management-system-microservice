import { Component, OnInit } from '@angular/core';
import { QuizService } from '../../../../services/quiz/quiz.service';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService, ConfirmationService } from 'primeng/api';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { Exam, ExamType } from '../../../../../model/quiz';

@Component({
  selector: 'app-exam-management',
  templateUrl: './exam-management.component.html',
  styleUrls: ['./exam-management.component.css'],
  providers: [MessageService, ConfirmationService]
})
export class ExamManagementComponent implements OnInit {
  courseId!: number;
  exams: Exam[] = [];
  loading: boolean = true;
  displayDialog: boolean = false;
  examForm: FormGroup;
  isEditMode: boolean = false;
  editingId: number | null = null;
  searchQuery!: string;

  examTypes = [
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
    this.examForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      examType: ['', Validators.required],
      duration: [60, [Validators.required, Validators.min(1)]],
      totalScore: [null],
      numberQuestions: [null, [Validators.min(1)]],
      numberSubmission: [1, [Validators.required, Validators.min(1)]],
      shuffleQuestions: [false],
      shuffleAnswers: [false],
      startTime: [null],
      endTime: [null]
    }, { validators: this.dateTimeValidator() });
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.courseId = params['courseId'];
      this.loadExams();
    });
  }

  loadExams(): void {
    this.loading = true;
    this.quizService.getAllExamsByCourseId(this.courseId).subscribe({
      next: (result) => {
        if (result.status === 1) {
          this.exams = result.data;
        } else {
          this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải danh sách bài kiểm tra' });
        }
        this.loading = false;
      },
      error: (error) => {
        this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải danh sách bài kiểm tra' });
        this.loading = false;
      }
    });
  }

  openNew(): void {
    this.isEditMode = false;
    this.editingId = null;
    this.examForm.reset({
      shuffleQuestions: false,
      shuffleAnswers: false,
      duration: 60,
      numberSubmission: 1
    });
    this.displayDialog = true;
  }

  editExam(exam: Exam): void {
    this.isEditMode = true;
    this.editingId = exam.id!;
    this.examForm.patchValue({
      name: exam.name,
      description: exam.description,
      examType: exam.examType,
      duration: exam.duration,
      totalScore: exam.totalScore,
      numberQuestions: exam.numberQuestions,
      numberSubmission: exam.numberSubmission || 1,
      shuffleQuestions: exam.shuffleQuestions,
      shuffleAnswers: exam.shuffleAnswers,
      startTime: exam.startTime ? new Date(exam.startTime) : null,
      endTime: exam.endTime ? new Date(exam.endTime) : null
    });
    this.displayDialog = true;
  }

  deleteExam(exam: Exam): void {
    this.confirmationService.confirm({
      message: 'Bạn có chắc chắn muốn xóa bài kiểm tra này?',
      header: 'Xác nhận xóa',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.quizService.deleteExam(exam.id).subscribe({
          next: (result) => {
            if (result.status === 1) {
              this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa bài kiểm tra thành công' });
              this.loadExams();
            } else {
              this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể xóa bài kiểm tra' });
            }
          },
          error: (error) => {
            this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể xóa bài kiểm tra' });
          }
        });
      }
    });
  }

  saveExam(): void {
    if (this.examForm.invalid) {
      Object.keys(this.examForm.controls).forEach(key => {
        this.examForm.get(key)?.markAsTouched();
      });

      if (this.hasTimeError()) {
        this.messageService.add({ 
          severity: 'error', 
          summary: 'Lỗi', 
          detail: 'Thời gian kết thúc phải sau thời gian bắt đầu' 
        });
      } else {
        this.messageService.add({ 
          severity: 'error', 
          summary: 'Lỗi', 
          detail: 'Vui lòng điền đầy đủ các trường bắt buộc' 
        });
      }
      return;
    }

    const examDTO: Exam = {
      ...this.examForm.value,
      courseId: this.courseId
    };

    if (this.isEditMode && this.editingId) {
      this.quizService.updateExam(this.editingId, examDTO).subscribe({
        next: (result) => {
          if (result.status === 1) {
            this.messageService.add({ 
              severity: 'success', 
              summary: 'Thành công', 
              detail: 'Cập nhật bài kiểm tra thành công'
            });
            this.displayDialog = false;
            this.loadExams();
          } else {
            this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể cập nhật bài kiểm tra' });
          }
        },
        error: (error) => {
          this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể cập nhật bài kiểm tra' });
        }
      });
    } else {
      this.quizService.createExam(examDTO).subscribe({
        next: (result) => {
          if (result.status === 1) {
            this.messageService.add({ 
              severity: 'success', 
              summary: 'Thành công', 
              detail: 'Tạo bài kiểm tra thành công'
            });
            this.displayDialog = false;
            this.loadExams();
          } else {
            this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tạo bài kiểm tra' });
          }
        },
        error: (error) => {
          this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tạo bài kiểm tra' });
        }
      });
    }
  }

  hideDialog(): void {
    this.displayDialog = false;
  }

  getExamTypeName(type: string): string {
    const found = this.examTypes.find(t => t.value === type);
    return found ? found.label : type;
  }

  navigateToExamDetail(exam: Exam): void {
    this.router.navigate(['/user/course', this.courseId, 'exam-detail', exam.id]);
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

  dateTimeValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const startTime = control.get('startTime')?.value;
      const endTime = control.get('endTime')?.value;
      
      if (!startTime || !endTime) {
        return null;
      }
      
      const startDate = new Date(startTime);
      const endDate = new Date(endTime);
      
      if (endDate <= startDate) {
        return { endDateBeforeStart: true };
      }
      
      return null;
    };
  }
  
  hasTimeError(): boolean {
    return this.examForm.errors?.['endDateBeforeStart'] && 
           this.examForm.get('startTime')?.value && 
           this.examForm.get('endTime')?.value;
  }
}
