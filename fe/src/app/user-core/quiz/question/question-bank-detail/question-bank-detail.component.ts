import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, FormArray } from '@angular/forms';
import { MessageService, ConfirmationService } from 'primeng/api';
import { QuizService } from '../../../../services/quiz/quiz.service';
import { QuestionType } from '../../../../../model/quiz';
import { Result } from '../../../../../model/result';

@Component({
  selector: 'app-question-bank-detail',
  templateUrl: './question-bank-detail.component.html',
  styleUrls: ['./question-bank-detail.component.css'],
  providers: [MessageService, ConfirmationService]
})
export class QuestionBankDetailComponent implements OnInit {
  questionBankId!: number;
  courseId!: number;
  questionBank: any = { // Initialize with default empty values
    id: 0,
    name: '',
    description: '',
    questionType: ''
  };
  questions: any[] = [];
  loading: boolean = true;
  
  questionForm!: FormGroup;
  displayQuestionDialog: boolean = false;
  isEditMode: boolean = false;
  editingQuestionId: number | null = null;
  currentQuestionType: string = '';
  
  isMultipleChoiceForm: boolean = false;
  isEssayForm: boolean = false;
  isCodingForm: boolean = false;
  
  difficultyLevels = [
    { label: 'Dễ', value: 'EASY' },
    { label: 'Trung bình', value: 'MEDIUM' },
    { label: 'Khó', value: 'HARD' }
  ];
  
  programmingLanguages = [
    { label: 'Java', value: 'java' },
    { label: 'Python', value: 'python' },
    { label: 'JavaScript', value: 'javascript' },
    { label: 'C++', value: 'cpp' }
  ];
  
  // Variables for file upload
  uploadDialogVisible: boolean = false;
  selectedFile: File | null = null;
  uploadProgress: number = 0;
  
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private quizService: QuizService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.questionBankId = params['id'];
      this.courseId = params['courseId'];
      this.loadQuestionBank();
    });
    
    if (!this.courseId) {
      this.route.parent?.params.subscribe(params => {
        this.courseId = params['courseId'];
      });
    }
    
    this.initializeForm();
  }
  
  loadQuestionBank(): void {
    this.loading = true;
    this.quizService.getQuestionBankByQuestionBankId(this.questionBankId).subscribe({
      next: (result: Result) => {
        if (result.status === 1 && result.data) {
          this.questionBank = result.data;
          // Only load questions after question bank is loaded
          this.loadQuestions();
        } else {
          this.messageService.add({ 
            severity: 'error', 
            summary: 'Lỗi', 
            detail: 'Không thể tải ngân hàng câu hỏi' 
          });
          this.loading = false;
        }
      },
      error: (error) => {
        this.messageService.add({ 
          severity: 'error', 
          summary: 'Lỗi', 
          detail: 'Không thể tải ngân hàng câu hỏi: ' + (error.message || 'Lỗi không xác định')
        });
        this.loading = false;
      }
    });
  }
  
  loadQuestions(): void {
    if (!this.questionBankId) {
      this.loading = false;
      return;
    }
    
    this.quizService.getQuestionsByQuestionBankId(this.questionBankId).subscribe({
      next: (result) => {
        if (result.status === 1) {
          this.questions = result.data || [];
          // Only update questionType if we have questions and questionBank is defined
          if (this.questions.length > 0 && this.questionBank) {
            this.currentQuestionType = this.questions[0].questionType;
            // Only update if not already set
            if (!this.questionBank.questionType) {
              this.questionBank.questionType = this.currentQuestionType;
            }
          }
        } else {
          this.messageService.add({ 
            severity: 'error', 
            summary: 'Lỗi', 
            detail: 'Không thể tải danh sách câu hỏi' 
          });
        }
        this.loading = false;
      },
      error: (error) => {
        this.messageService.add({ 
          severity: 'error', 
          summary: 'Lỗi', 
          detail: 'Không thể tải danh sách câu hỏi: ' + (error.message || 'Lỗi không xác định')
        });
        this.loading = false;
      }
    });
  }
  
  initializeForm(): void {
    // Base form for all question types
    this.questionForm = this.fb.group({
      text: ['', Validators.required],
      points: [1, [Validators.required, Validators.min(1)]],
      difficultyLevel: ['MEDIUM', Validators.required],
      questionBankId: [this.questionBankId],
      // Additional fields will be added dynamically based on question type
    });
  }
  
  openNewQuestion(): void {
    if (!this.questionBank || !this.questionBank.questionType) {
      this.messageService.add({ 
        severity: 'error', 
        summary: 'Lỗi', 
        detail: 'Loại câu hỏi chưa được xác định' 
      });
      return;
    }
    
    this.isEditMode = false;
    this.editingQuestionId = null;
    this.initializeForm();
    
    this.setupFormForQuestionType(this.questionBank.questionType);
    this.displayQuestionDialog = true;
  }
  
  setupFormForQuestionType(type: string): void {
    // Reset form type flags
    this.isMultipleChoiceForm = false;
    this.isEssayForm = false;
    this.isCodingForm = false;
    
    // Setup form based on question type
    switch(type) {
      case 'MULTIPLE_CHOICE':
        this.isMultipleChoiceForm = true;
        this.setupMultipleChoiceForm();
        break;
      case 'ESSAY':
        this.isEssayForm = true;
        // Essay questions don't need additional fields
        break;
      case 'CODING':
        this.isCodingForm = true;
        this.setupCodingForm();
        break;
    }
  }
  
  setupMultipleChoiceForm(): void {
    this.questionForm.addControl('allowMultipleAnswers', this.fb.control(false));
    this.questionForm.addControl('options', this.fb.array([]));
    // Add at least two options by default
    this.addOption();
    this.addOption();
    
    // Subscribe to changes on allowMultipleAnswers to enforce validation
    this.questionForm.get('allowMultipleAnswers')?.valueChanges.subscribe(value => {
      if (!value) {
        this.enforceOneCorrectOption();
      }
    });
  }

  enforceOneCorrectOption(): void {
    let foundCorrect = false;
    
    for (let i = 0; i < this.optionsArray.length; i++) {
      const option = this.optionsArray.at(i);
      const isCorrect = option.get('isCorrect')?.value;
      
      if (isCorrect) {
        if (foundCorrect) {
          // If we already found a correct option, set this one to false
          option.get('isCorrect')?.setValue(false);
        } else {
          foundCorrect = true;
        }
      }
    }
  }
  
  validateMultipleChoiceOptions(): boolean {
    const options = this.optionsArray.value;
    const allowMultipleAnswers = this.questionForm.get('allowMultipleAnswers')?.value;
    
    // Check if at least one option is marked as correct
    const correctOptionsCount = options.filter((option: any) => option.isCorrect).length;
    
    if (correctOptionsCount === 0) {
      this.messageService.add({
        severity: 'error',
        summary: 'Lỗi',
        detail: 'Phải có ít nhất một lựa chọn đúng'
      });
      return false;
    }
    
    // If not allowing multiple answers, ensure only one option is marked as correct
    if (!allowMultipleAnswers && correctOptionsCount > 1) {
      this.messageService.add({
        severity: 'error',
        summary: 'Lỗi',
        detail: 'Chỉ được phép một lựa chọn đúng khi không cho phép nhiều câu trả lời'
      });
      return false;
    }
    
    return true;
  }
  
  setupCodingForm(): void {
    this.questionForm.addControl('programmingLanguage', this.fb.control('java', Validators.required));
    this.questionForm.addControl('starterCode', this.fb.control(''));
    this.questionForm.addControl('solutionCode', this.fb.control('', Validators.required));
    this.questionForm.addControl('testCases', this.fb.control('', Validators.required));
  }
  
  get optionsArray(): FormArray {
    return this.questionForm.get('options') as FormArray;
  }
  
  addOption(): void {
    const option = this.fb.group({
      text: ['', Validators.required],
      isCorrect: [false],
      displayOrder: [this.optionsArray.length]
    });
    this.optionsArray.push(option);
  }
  
  removeOption(index: number): void {
    this.optionsArray.removeAt(index);
    // Update display order
    for (let i = 0; i < this.optionsArray.length; i++) {
      this.optionsArray.at(i).get('displayOrder')?.setValue(i);
    }
  }
  
  editQuestion(question: any): void {
    this.isEditMode = true;
    this.editingQuestionId = question.id;
    this.initializeForm();
    
    // Set up form based on question type
    this.setupFormForQuestionType(question.questionType);
    
    // Patch common values
    this.questionForm.patchValue({
      text: question.text,
      points: question.points,
      difficultyLevel: question.difficultyLevel,
      questionBankId: this.questionBankId
    });
    
    // Patch type-specific values
    if (question.questionType === 'MULTIPLE_CHOICE') {
      this.questionForm.patchValue({
        allowMultipleAnswers: question.allowMultipleAnswers
      });
      
      // Clear default options
      while (this.optionsArray.length) {
        this.optionsArray.removeAt(0);
      }
      
      // Add existing options
      if (question.options && question.options.length) {
        question.options.forEach((option: any) => {
          this.optionsArray.push(this.fb.group({
            text: [option.text, Validators.required],
            isCorrect: [option.isCorrect],
            displayOrder: [option.displayOrder]
          }));
        });
      }
    } else if (question.questionType === 'CODING') {
      this.questionForm.patchValue({
        programmingLanguage: question.programmingLanguage,
        starterCode: question.starterCode,
        solutionCode: question.solutionCode,
        testCases: question.testCases
      });
    }
    
    this.displayQuestionDialog = true;
  }
  
  saveQuestion(): void {
    if (this.questionForm.invalid) {
      this.messageService.add({ 
        severity: 'error', 
        summary: 'Lỗi', 
        detail: 'Vui lòng điền đầy đủ các trường bắt buộc' 
      });
      return;
    }
    
    // Validate options for multiple choice questions
    if (this.isMultipleChoiceForm && !this.validateMultipleChoiceOptions()) {
      return;
    }
    
    const questionData = this.questionForm.value;
    
    if (this.isEditMode && this.editingQuestionId) {
      // Update existing question
      if (this.isMultipleChoiceForm) {
        this.quizService.updateMultipleChoiceQuestion(this.editingQuestionId, questionData).subscribe(this.handleResponse);
      } else if (this.isEssayForm) {
        this.quizService.updateEssayQuestion(this.editingQuestionId, questionData).subscribe(this.handleResponse);
      } else if (this.isCodingForm) {
        this.quizService.updateCodingQuestion(this.editingQuestionId, questionData).subscribe(this.handleResponse);
      }
    } else {
      // Create new question
      if (this.isMultipleChoiceForm) {
        this.quizService.addMultipleChoiceQuestion(questionData).subscribe(this.handleResponse);
      } else if (this.isEssayForm) {
        this.quizService.addEssayQuestion(questionData).subscribe(this.handleResponse);
      } else if (this.isCodingForm) {
        this.quizService.addCodingQuestion(questionData).subscribe(this.handleResponse);
      }
    }
  }
  
  handleResponse = (result: any) => {
    if (result.status === 1) {
      this.messageService.add({ 
        severity: 'success', 
        summary: 'Thành công', 
        detail: this.isEditMode ? 'Cập nhật câu hỏi thành công' : 'Thêm câu hỏi thành công' 
      });
      this.displayQuestionDialog = false;
      this.loadQuestions();
    } else {
      this.messageService.add({ 
        severity: 'error', 
        summary: 'Lỗi', 
        detail: 'Không thể lưu câu hỏi' 
      });
    }
  }

  downloadEssayQuestionTemplate(): void {
    this.quizService.downloadEssayQuestionsTemplate().subscribe(
      (response: Blob) => {
        // Create a blob URL for the file
        const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
        const url = window.URL.createObjectURL(blob);
        
        // Create a temporary link element
        const link = document.createElement('a');
        link.href = url;
        link.download = 'essay_question_template.xlsx'; // Set filename for download
        
        // Append to body, click to trigger download, then remove
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        // Clean up by revoking the blob URL
        window.URL.revokeObjectURL(url);
        
        this.messageService.add({ 
          severity: 'success', 
          summary: 'Thành công', 
          detail: 'Tải mẫu câu hỏi thành công' 
        });
      }, 
      (error) => {
        this.messageService.add({ 
          severity: 'error', 
          summary: 'Lỗi', 
          detail: 'Không thể tải mẫu câu hỏi' 
        });
      }
    );
  }

  downloadMultipleChoiceQuestionTemplate(): void {
    this.quizService.downloadMultipleChoiceQuestionsTemplate().subscribe(
      (response: Blob) => {
        // Create a blob URL for the file
        const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
        const url = window.URL.createObjectURL(blob);
        
        // Create a temporary link element
        const link = document.createElement('a');
        link.href = url;
        link.download = 'multiple_choice_question_template.xlsx'; // Set filename for download
        
        // Append to body, click to trigger download, then remove
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        // Clean up by revoking the blob URL
        window.URL.revokeObjectURL(url);
        
        this.messageService.add({ 
          severity: 'success', 
          summary: 'Thành công', 
          detail: 'Tải mẫu câu hỏi trắc nghiệm thành công' 
        });
      }, 
      (error) => {
        this.messageService.add({ 
          severity: 'error', 
          summary: 'Lỗi', 
          detail: 'Không thể tải mẫu câu hỏi trắc nghiệm' 
        });
      }
    );
  }

  uploadMultipleChoiceFile(): void {
    if (!this.selectedFile) {
      this.messageService.add({ 
        severity: 'error', 
        summary: 'Lỗi', 
        detail: 'Vui lòng chọn file để tải lên' 
      });
      return;
    }
    
    this.uploadProgress = 50; // Simulate progress
    
    this.quizService.importMultipleChoiceQuestionsFromExcel(this.selectedFile, this.questionBankId).subscribe({
      next: (result) => {
        this.uploadProgress = 100;
        if (result.status === 1) {
          this.messageService.add({ 
            severity: 'success', 
            summary: 'Thành công', 
            detail: 'Nhập câu hỏi trắc nghiệm từ Excel thành công' 
          });
          this.uploadDialogVisible = false;
          this.loadQuestions();
        } else {
          this.messageService.add({ 
            severity: 'error', 
            summary: 'Lỗi', 
            detail: result.message || 'Không thể nhập câu hỏi trắc nghiệm từ Excel' 
          });
        }
      },
      error: (error) => {
        this.uploadProgress = 0;
        this.messageService.add({ 
          severity: 'error', 
          summary: 'Lỗi', 
          detail: 'Không thể nhập câu hỏi trắc nghiệm từ Excel' 
        });
      }
    });
  }
  
  hideDialog(): void {
    this.displayQuestionDialog = false;
  }
  
  deleteQuestion(question: any): void {
    this.confirmationService.confirm({
      message: 'Bạn có chắc chắn muốn xóa câu hỏi này?',
      header: 'Xác nhận xóa',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.quizService.deleteQuestion(question.id).subscribe({
          next: (result) => {
            if (result.status === 1) {
              this.messageService.add({ 
                severity: 'success', 
                summary: 'Thành công', 
                detail: 'Đã xóa câu hỏi thành công' 
              });
              this.loadQuestions();
            } 
          },
          error: (error) => {
            this.messageService.add({ 
              severity: 'error', 
              summary: 'Lỗi', 
              detail: 'Không thể xóa câu hỏi' 
            });
          }
        })
      },
    
    });

  }
  
  getDifficultyLabel(difficulty: string): string {
    const found = this.difficultyLevels.find(d => d.value === difficulty);
    return found ? found.label : difficulty;
  }
  
  // File upload methods
  openUploadDialog(): void {
    this.selectedFile = null;
    this.uploadProgress = 0;
    this.uploadDialogVisible = true;
  }
  
  onFileSelected(event: any): void {
    this.selectedFile = event.files[0];
  }
  
  uploadFile(): void {
    if (!this.selectedFile) {
      this.messageService.add({ 
        severity: 'error', 
        summary: 'Lỗi', 
        detail: 'Vui lòng chọn file để tải lên' 
      });
      return;
    }
    
    this.uploadProgress = 50; // Simulate progress
    
    this.quizService.importEssayQuestionsFromExcel(this.selectedFile, this.questionBankId).subscribe({
      next: (result) => {
        this.uploadProgress = 100;
        if (result.status === 1) {
          this.messageService.add({ 
            severity: 'success', 
            summary: 'Thành công', 
            detail: 'Nhập câu hỏi từ Excel thành công' 
          });
          this.uploadDialogVisible = false;
          this.loadQuestions();
        } else {
          this.messageService.add({ 
            severity: 'error', 
            summary: 'Lỗi', 
            detail: result.message || 'Không thể nhập câu hỏi từ Excel' 
          });
        }
      },
      error: (error) => {
        this.uploadProgress = 0;
        this.messageService.add({ 
          severity: 'error', 
          summary: 'Lỗi', 
          detail: 'Không thể nhập câu hỏi từ Excel' 
        });
      }
    });
  }
  
  goBack(): void {
    this.router.navigate(['/user/course', this.courseId, 'question-bank-management']);
  }
}
