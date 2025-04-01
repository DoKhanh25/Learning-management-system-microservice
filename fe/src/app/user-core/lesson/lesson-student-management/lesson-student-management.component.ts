import { Component, OnInit } from '@angular/core';
  import { CourseService } from '../../../services/course/course.service';
  import { UserService } from '../../../services/user-service/user.service';
  import { ActivatedRoute } from '@angular/router';
  import { forkJoin } from 'rxjs';
  import { MessageService } from 'primeng/api';

  @Component({
    selector: 'app-lesson-student-management',
    templateUrl: './lesson-student-management.component.html',
    styleUrls: ['./lesson-student-management.component.css'],
    providers: [MessageService]
  })
  export class LessonStudentManagementComponent implements OnInit {
    courseId: any;
    courseName: string = '';
    selectedSection: any;
    selectedLesson: any;
    selectedStudent: any;
    sections: any[] = [];
    lessons: any[] = [];
    sectionLessons: any[] = [];
    students: any[] = [];
    studentProgress: any[] = [];
    totalTimeSpent: number = 0;
    loading: boolean = false;

    constructor(
      private courseService: CourseService,
      private userService: UserService,
      private route: ActivatedRoute,
      private messageService: MessageService
    ) { }

    ngOnInit(): void {
      this.route.params.subscribe(params => {
        this.courseId = params['id'];
        if (this.courseId) {
          this.loadCourseData();
          this.loadCourseStudents();
        }
      });
    }

    loadCourseData() {
      this.loading = true;
      this.courseService.getCourseById(this.courseId).subscribe({
        next: (response: any) => {
          if (response.status === 1 && response.data) {
            this.courseName = response.data.name;
            if (response.data.courseSections) {
              this.sections = response.data.courseSections.sort((a: any, b: any) => a.section - b.section);

              // Load all lessons for each section
              this.lessons = [];
              this.sections.forEach(section => {
                if (section.lessons && section.lessons.length > 0) {
                  section.lessons.forEach((lesson: any) => {
                    this.lessons.push({
                      ...lesson,
                      sectionName: section.name,
                      sectionId: section.id,
                      displayName: `${section.name} - ${lesson.name}`
                    });
                  });
                }
              });
            }
          }
          this.loading = false;
        },
        error: (error) => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load course data' });
          this.loading = false;
        }
      });
    }

    loadCourseStudents() {
      this.loading = true;
      this.courseService.getAllStudentEnrolmentsByCourseId(this.courseId).subscribe({
        next: (response: any) => {
          if (response.status === 1 && response.data) {
            const userIds = response.data.map((enrollment: any) => enrollment.userId);

            if (userIds.length > 0) {
              this.userService.getUsersByIds(userIds).subscribe({
                next: (userResponse: any) => {
                  if (userResponse.status === 1 && userResponse.data) {
                    this.students = userResponse.data.map((user: any) => ({
                      id: user.userId,
                      name: `${user.firstName} ${user.lastName}`,
                      email: user.email
                    }));
                  }
                  this.loading = false;
                },
                error: (error) => {
                  this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load student details' });
                  this.loading = false;
                }
              });
            } else {
              this.loading = false;
            }
          } else {
            this.loading = false;
          }
        },
        error: (error) => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load students' });
          this.loading = false;
        }
      });
    }

    onSectionChange() {
      if (this.selectedSection) {
        this.loading = true;
        this.courseService.findLessonEntitiesBySectionId(this.selectedSection.id).subscribe({
          next: (response: any) => {
            if (response.status === 1 && response.data) {
              this.sectionLessons = response.data.map((lesson: any) => ({
                ...lesson,
                sectionName: this.selectedSection.name,
                displayName: `${this.selectedSection.name} - ${lesson.name}`
              }));
            } else {
              this.sectionLessons = [];
            }
            this.selectedLesson = null;
            this.studentProgress = [];
            this.loading = false;
          },
          error: (error) => {
            this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load lessons' });
            this.loading = false;
          }
        });
      }
    }

    onLessonChange() {
      if (this.selectedLesson && this.selectedStudent) {
        this.loadStudentProgress();
      }
    }

    onStudentChange() {
      if (this.selectedLesson && this.selectedStudent) {
        this.loadStudentProgress();
      }
    }

    loadStudentProgress() {
      this.loading = true;
      console.log(this.selectedStudent)
      this.courseService.getStudentProgressByLessonIdAndUserId(this.selectedLesson.id, this.selectedStudent.id).subscribe({
        next: (response: any) => {
          if (response.status === 1 && response.data) {
            this.studentProgress = response.data;
            this.calculateTotalTime();
          } else {
            this.studentProgress = [];
            this.totalTimeSpent = 0;
          }
          this.loading = false;
        },
        error: (error) => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load student progress' });
          this.loading = false;
        }
      });
    }

    calculateTotalTime() {
      this.totalTimeSpent = this.studentProgress.reduce((total, progress) => {
        return total + (progress.timeSeen || 0);
      }, 0);
    }

    formatTime(seconds: number): string {
      if (!seconds) return '0 seconds';

      const minutes = Math.floor(seconds / 60);
      const hours = Math.floor(minutes / 60);

      if (hours > 0) {
        return `${hours}h ${minutes % 60}m ${seconds % 60}s`;
      } else if (minutes > 0) {
        return `${minutes}m ${seconds % 60}s`;
      } else {
        return `${seconds}s`;
      }
    }

   
  }
