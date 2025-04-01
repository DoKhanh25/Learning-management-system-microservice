import { Component, OnInit } from '@angular/core';
    import { ActivatedRoute, Router } from '@angular/router';
    import { CourseService } from '../../../services/course/course.service';
    import { Assignment, AssignmentSubmission } from '../../../../model/assignment';
    import { FormBuilder, FormGroup, Validators } from '@angular/forms';
    import { MessageService } from 'primeng/api';
    import { MenuItem } from 'primeng/api';
    import {UserService} from "../../../services/user-service/user.service";

    @Component({
      selector: 'app-assignment-management',
      templateUrl: './assignment-management.component.html',
      styleUrl: './assignment-management.component.css'
    })
    export class AssignmentManagementComponent implements OnInit {
      assignmentId!: any;
      assignment!: Assignment;
      submissions: AssignmentSubmission[] = [];
      loading = false;
      selectedSubmission!: AssignmentSubmission;
      gradeForm!: FormGroup;
      userMap: {[key: string]: any} = {}; // Map to store user data

      // Breadcrumb configuration
      breadcrumbItems: MenuItem[] = [];
      home: MenuItem = { icon: 'pi pi-home', routerLink: '/user/dashboard' };

      constructor(
        private route: ActivatedRoute,
        private router: Router,
        private courseService: CourseService,
        private fb: FormBuilder,
        private messageService: MessageService,
        private userService: UserService
      ) { }

      ngOnInit(): void {
        this.assignmentId = this.route.snapshot.paramMap.get('id') || null;
        if(this.assignmentId != null){
          this.loadAssignmentDetails();
          this.loadSubmissions();
        }

        this.gradeForm = this.fb.group({
          grade: [null, [Validators.required, Validators.min(0), Validators.max(10)]],
          comment: ['']
        });
      }

      loadAssignmentDetails() {
        this.loading = true;
        this.courseService.getAssignmentById(this.assignmentId).subscribe({
          next: (result) => {
            if (result && result.status === 1) {
              this.assignment = result.data;
              this.setupBreadcrumbs();
            }
            this.loading = false;
          },
          error: (err) => {
            console.error('Error loading assignment details', err);
            this.loading = false;
            this.messageService.add({
              severity: 'error',
              summary: 'Lỗi',
              detail: 'Không thể tải thông tin bài tập'
            });
          }
        });
      }

      setupBreadcrumbs() {
        if (this.assignment && this.assignment.course) {
          this.breadcrumbItems = [
            { label: 'Khóa học', routerLink: '/user/courses' },
            { label: this.assignment.course.name, routerLink: `/user/course-detail/${this.assignment.courseId}` },
            { label: 'Quản lý bài tập', routerLink: `/user/course-detail/${this.assignment.courseId}` },
            { label: this.assignment.name }
          ];
        }
      }

      loadSubmissions() {
        this.loading = true;
        this.courseService.getAssignmentSubmissions(this.assignmentId).subscribe({
          next: (result) => {
            if (result && result.status === 1) {
              this.submissions = result.data || [];
              this.loadUserData(); // Load user data after submissions are loaded

            }
            this.loading = false;
          },
          error: (err) => {
            console.error('Error loading submissions', err);
            this.loading = false;
            this.messageService.add({
              severity: 'error',
              summary: 'Lỗi',
              detail: 'Không thể tải danh sách bài nộp'
            });
          }
        });
      }

      viewSubmission(submission: AssignmentSubmission) {
        this.selectedSubmission = submission;
        this.gradeForm.patchValue({
          grade: submission.grade,
          comment: submission.submissionComment
        });
      }

      loadUserData() {
        if (!this.submissions.length) return;

        // Get unique user IDs from submissions
        const userIds = [...new Set(this.submissions.map(s => s.userId))];

        // If there are user IDs, fetch their information
        if (userIds.length) {
          this.userService.getUsersByIds(userIds).subscribe({
            next: (response) => {
              if (response && response.status === 1) {
                const users = response.data || [];
                users.forEach((user: any) => {
                  this.userMap[user.userId] = user;
                });
              }
            },
            error: (err) => {
              console.error('Error loading user data', err);
            }
          });
        }
      }
      getUserName(userId: string): string {
        console.log(this.userMap)
        return this.userMap[userId]?.fullName || this.userMap[userId]?.username || userId;
      }

      downloadFile(submission: AssignmentSubmission, fileIndex: number) {
        this.loading = true;
        this.courseService.downloadSubmissionFile(submission.id!, fileIndex).subscribe({
          next: (blob) => {
            // Get the file names from the submission
            const fileNames = submission.data1?.split(';') || [];
            const fileName = fileNames[fileIndex] || `file-${fileIndex}.pdf`;

            // Create a URL for the blob and trigger download
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = fileName;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
            this.loading = false;
          },
          error: (err) => {
            console.error('Error downloading file', err);
            this.messageService.add({
              severity: 'error',
              summary: 'Lỗi',
              detail: 'Không thể tải xuống tập tin'
            });
            this.loading = false;
          }
        });
      }

      submitGrade() {
        if (this.gradeForm.invalid) {
          return;
        }

        const gradeData = {
          id: this.selectedSubmission.id,
          grade: this.gradeForm.value.grade,
          submissionComment: this.gradeForm.value.comment
        };

        this.courseService.updateSubmissionGrade(gradeData).subscribe({
          next: (result) => {
            if (result && result.status === 1) {
              this.messageService.add({
                severity: 'success',
                summary: 'Thành công',
                detail: 'Đã cập nhật điểm và phản hồi'
              });
              this.loadSubmissions();
            }
          },
          error: (err) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Lỗi',
              detail: 'Không thể cập nhật điểm'
            });
          }
        });
      }

      getFileNames(submission: AssignmentSubmission): string[] {
        return submission?.data1?.split(';') || [];
      }
    }
