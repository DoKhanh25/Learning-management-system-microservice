import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MenuItem, MessageService } from 'primeng/api';
import { CourseService } from '../../../services/course/course.service';
import { Assignment } from '../../../../model/assignment';

@Component({
  selector: 'app-course-detail',
  templateUrl: './course-detail.component.html',
  styleUrl: './course-detail.component.css'
})
export class CourseDetailComponent implements OnInit {
  courseId: string | null = null;
  course: any = null;
  sections: any[] = [];
  loading = false;
  items: MenuItem[] = [];
  expandedSections: {[key: number]: boolean} = {};
  assignments: Assignment[] = [];

  constructor(
    private route: ActivatedRoute,
    private courseService: CourseService,
    private messageService: MessageService,
    private router: Router
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
      { label: 'My Courses', routerLink: '/student/courses' },
      { label: 'Course Details' }
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

  toggleSection(sectionId: number) {
    this.expandedSections[sectionId] = !this.expandedSections[sectionId];
  }

  navigateToLessonDetail(lessonId: any) {
    this.router.navigate(['/student/lesson-detail', lessonId]);
  }

   navigateToAssignmentDetail(assignmentId: any) {
    this.router.navigate(['/student/assignment-detail', assignmentId]);
  }
}
