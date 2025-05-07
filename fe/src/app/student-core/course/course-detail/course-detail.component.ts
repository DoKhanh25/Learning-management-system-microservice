import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MenuItem, MessageService, ConfirmationService } from 'primeng/api';
import { CourseService } from '../../../services/course/course.service';
import { Assignment } from '../../../../model/assignment';
import { QuizService } from '../../../services/quiz/quiz.service';

@Component({
  selector: 'app-course-detail',
  templateUrl: './course-detail.component.html',
  styleUrl: './course-detail.component.css',
  providers: [ConfirmationService]
})
export class CourseDetailComponent implements OnInit {
  courseId: string | null = null;
  course: any = null;
  sections: any[] = [];
  loading = false;
  items: MenuItem[] = [];
  expandedSections: { [key: number]: boolean } = {};
  assignments: Assignment[] = [];
  availableExams: any[] = [];

  constructor(
    private route: ActivatedRoute,
    private courseService: CourseService,
    private messageService: MessageService,
    private router: Router,
    private quizService: QuizService,
    private confirmationService: ConfirmationService
  ) {}

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      this.courseId = params.get('id');
      if (this.courseId) {
        this.loadCourseData();
      }
    });

    this.setupBreadcrumb();
  }

  private setupBreadcrumb() {
    this.items = [
      { icon: 'pi pi-home', routerLink: '/student/home' },
      { label: 'Chi tiết khóa học' }
    ];
  }

  loadCourseData() {
    if (!this.courseId) return;

    this.loading = true;
    this.courseService.getTeacherCourseById(this.courseId).subscribe({
      next: (result) => {
        if (result.status === 1) {
          this.course = result.data;
          this.sections = this.course.courseSections || [];

          // Initialize expanded state for all sections
          this.sections.forEach(section => {
            this.expandedSections[section.id] = false;
          });

          this.loadAssignments();
          this.loadAvailableExams();
        }
        this.loading = false;
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load course data',
          life: 3000
        });
        this.loading = false;
      }
    });
  }

  loadAssignments() {
    if (!this.courseId) return;

    this.courseService.getAllAssignmentsByCourseId(this.courseId).subscribe({
      next: (response) => {
        if (response.status === 1) {
          this.assignments = response.data || [];
        }
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load assignments',
          life: 3000
        });
      }
    });
  }

  loadAvailableExams() {
    if (!this.courseId) return;

    this.quizService.getAvailableExamsByCourseId(this.courseId).subscribe({
      next: (response) => {
        if (response.status === 1) {
          this.availableExams = response.data || [];
        }
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load exams',
          life: 3000
        });
      }
    });
  }

  checkStatusDate(startDate: string, endDate: string): string {
    const currentDate = new Date();
    const start = new Date(startDate);
    const end = new Date(endDate);

    if (currentDate < start) {
      return 'Not Started';
    } else if (currentDate >= start && currentDate <= end) {
      return 'In Progress';
    } else {
      return 'Overdue';
    }
  }

  getExamStatus(startTime: string, endTime: string): string {
    const currentDate = new Date();
    const start = new Date(startTime);
    const end = new Date(endTime);

    if (currentDate < start) {
      return 'Not Started';
    } else if (currentDate >= start && currentDate <= end) {
      return 'Available';
    } else {
      return 'Closed';
    }
  }

  toggleSection(sectionId: number) {
    this.expandedSections[sectionId] = !this.expandedSections[sectionId];
  }

  navigateToLessonDetail(lessonId: any) {
    this.router.navigate(['/student/lesson-detail', lessonId]);
  }

  navigateToAssignmentDetail(assignmentId: any) {
    this.router.navigate(['/student/assignment-detail', assignmentId]);
  }

  startExam(exam: any) {
    this.confirmationService.confirm({
      key: 'startExamDialog',
      header: 'Start Exam: ' + exam.name,
      message: `
        <div class="exam-confirmation">
          <p><strong>Thời gian:</strong> ${exam.duration} minutes</p>
          <p><strong>Số lần làm bài:</strong> ${exam.numberSubmission}</p>
          <hr>
          <p class="warning">Khi bạn bắt đầu làm bài, bài kiểm tra sẽ mở toàn màn hình. Nếu bạn mở tab khác hệ thống sẽ tự nộp bài</p>
        </div>
      `,
      accept: () => {
        this.quizService.startExam(exam.id).subscribe({
          next: (response) => {
            if (response.status === 1) {
              // Route based on exam type
              if (exam.examType === 'ESSAY') {
                this.router.navigate(['/student/essay-exam', response.data.id]);
              } else if(exam.examType === 'MULTIPLE_CHOICE') {
                this.router.navigate(['/student/exam', response.data.id]);
              } else {
                this.router.navigate(['/student/coding-exam', response.data.id]);
              }
            } else {
              this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: response.message || 'Failed to start exam',
                life: 3000
              });
            }
          },
          error: (error) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Failed to start exam',
              life: 3000
            });
          }
        });
      }
    });
  }
}
