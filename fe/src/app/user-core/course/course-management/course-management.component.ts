import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import {CourseService} from "../../../services/course/course.service";
import {UserService} from "../../../services/user-service/user.service";
import {forkJoin} from "rxjs";
import {User} from "../../../../model/user";

@Component({
  selector: 'app-course-management',
  templateUrl: './course-management.component.html',
  styleUrl: './course-management.component.css'
})
export class CourseManagementComponent implements OnInit {
  courseId: number | null = null;
  courseName: string = '';
  enrolledUsers: User[] = [];
  selectedUser: any = null;
  userProgress: any = null;
  loading: boolean = true;

  constructor(
    private courseService: CourseService,
    private route: ActivatedRoute,
    private messageService: MessageService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.courseId = +params['id'];
        this.loadCourseInfo();
        this.loadEnrolledUsers();
      }
    });
  }

  loadCourseInfo(): void {
    if (!this.courseId) return;

    this.courseService.getCourseById(this.courseId).subscribe({
      next: (result) => {
        if (result.status === 1 && result.data) {
          this.courseName = result.data.name;
        }
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load course information'
        });
      }
    });
  }

  loadEnrolledUsers(): void {
    if (!this.courseId) return;

    this.loading = true;
    forkJoin({
        userEnrols: this.courseService.getAllUserEnrolmentsByCourseId(this.courseId),
        allUsers: this.userService.getAllUsers()}
    ).subscribe({
      next: (response) => {
        if(response.userEnrols.status === 1 && response.allUsers.status === 1){
          const enrolledUserIds = response.userEnrols.data.map((enrol: any) => enrol.userId);
          this.enrolledUsers = response.allUsers.data.filter((user: any) =>
            enrolledUserIds.includes(user.userId)
          );
          this.loading = false;
        }
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load enrolled users'
        });
        this.loading = false;
      }
    });
    this.courseService.getAllStudentEnrolmentsByCourseId(this.courseId).subscribe({
      next: (result) => {
        if (result.status === 1 && result.data) {
          this.enrolledUsers = result.data;
        }
        this.loading = false;
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load enrolled users'
        });
        this.loading = false;
      }
    });
  }

  viewUserProgress(user: any): void {
    this.selectedUser = user;
    this.loadUserProgressDetails(user.id);
  }

  loadUserProgressDetails(userId: string): void {
    if (!this.courseId) return;

    this.loading = true;

    // this.courseService.getUserProgress(this.courseId, userId).subscribe({
    //   next: (result) => {
    //     if (result.status === 1 && result.data) {
    //       this.userProgress = result.data;
    //     }
    //     this.loading = false;
    //   },
    //   error: (err) => {
    //     this.messageService.add({
    //       severity: 'error',
    //       summary: 'Error',
    //       detail: 'Failed to load user progress'
    //     });
    //     this.loading = false;
    //   }
    // });
  }
  getUserLessonPages(lessonId: number): any[] {
    if (!this.userProgress || !this.userProgress.lessonBranches) {
      return [];
    }

    return this.userProgress.lessonBranches.filter((branch: any) =>
      branch.lesson && branch.lesson.id === lessonId
    );
  }
}
