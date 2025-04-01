import { Component, OnInit } from '@angular/core';
import { CourseService } from '../../../services/course/course.service';
import { MessageService, ConfirmationService } from 'primeng/api';
import { Router } from '@angular/router';
import { Course, Enrolment } from '../../../../model/course';
import { FormControl } from '@angular/forms';
import { debounceTime } from 'rxjs/operators';

@Component({
  selector: 'app-course-list',
  templateUrl: './course-list.component.html',
  styleUrl: './course-list.component.css',
  providers: [MessageService, ConfirmationService]
})
export class CourseListComponent implements OnInit {
  courses: Course[] = [];
  filteredCourses: any[] = [];
  loading: boolean = true;
  displayEnrollDialog: boolean = false;
  selectedCourse: any;
  enrollPassword: string = '';

  searchControl = new FormControl('');
  selectedRole: string = 'STUDENT';  // Default role
  courseRoles: any[] = [
    { label: 'Student', value: 'STUDENT' },
    { label: 'Teacher', value: 'TEACHER' }
  ];

  constructor(
    private courseService: CourseService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private router: Router
  ) { }

  ngOnInit() {
    this.loadCourses();
    this.setupSearch();
  }

  setupSearch() {
    this.searchControl.valueChanges
      .pipe(debounceTime(300))
      .subscribe(value => {
        this.filterCourses(value || '');
      });
  }

  filterCourses(query: string) {
    if (!query.trim()) {
      this.filteredCourses = [...this.courses];
      return;
    }

    query = query.toLowerCase();
    this.filteredCourses = this.courses.filter(course =>
      course.name?.toLowerCase().includes(query) ||
      course.summary?.toLowerCase().includes(query)
    );
  }

  loadCourses() {
    this.loading = true;
    this.courseService.getAllCourseAvailable().subscribe(
      (result) => {
        if (result.status == 1) {
          this.courses = result.data;
          this.filteredCourses = [...this.courses];
        }
        this.loading = false;
      },
      (error) => {
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load courses',
          life: 3000
        });
      }
    );
  }

  openEnrollDialog(course: any) {
    this.selectedCourse = course;
    this.enrollPassword = '';
    this.selectedRole = 'STUDENT'; // Reset to default
    this.displayEnrollDialog = true;
  }

  enrollCourse() {
    if (!this.selectedCourse) return;

    const enrollment: any = {
      course: this.selectedCourse.id,
      password: this.enrollPassword,
      courseRole: this.selectedRole
    };

    this.courseService.addSelfUserEnrolment(enrollment).subscribe(
      (response) => {
        if (response.status === 1) {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Successfully enrolled in course',
            life: 3000
          });
          this.displayEnrollDialog = false;
          this.loadCourses();
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: response.message || 'Failed to enroll in course',
            life: 3000
          });
        }
      },
      (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to enroll in course',
          life: 3000
        });
      }
    );
  }



  formatDate(date: any): string {
    if (!date) return 'N/A';
    const d = new Date(date);
    return d.toLocaleDateString();
  }

  getRandomColor(courseName: string | null = ''): string {
    console.log(courseName)
    // Handle null or undefined courseName
    const name = courseName || '';
    const colors = ['#3498db', '#2ecc71', '#e74c3c', '#f39c12', '#9b59b6', '#1abc9c'];
    const index = Math.abs(name.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0) % colors.length);
    return colors[index];
  }
}
