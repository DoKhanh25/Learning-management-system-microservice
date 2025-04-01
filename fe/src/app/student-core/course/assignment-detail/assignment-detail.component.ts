import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { CourseService } from '../../../services/course/course.service';
import { Assignment, AssignmentSubmission } from '../../../../model/assignment';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import {AuthService} from "../../../services/auth/auth.service";
import {blob} from "node:stream/consumers";

@Component({
  selector: 'app-assignment-detail',
  templateUrl: './assignment-detail.component.html',
  styleUrl: './assignment-detail.component.css'
})
export class AssignmentDetailComponent implements OnInit {
  assignmentId: string | null = null;
  assignment: Assignment | null = null;
  submission: AssignmentSubmission | null = null;
  loading = false;
  submitting = false;
  textForm: FormGroup;
  uploadedFiles: File[] = [];
  userId: any;
  uploadedFileInfo: { originalName: string, index: number, submissionId?: number }[] = [];

  constructor(
    private route: ActivatedRoute,
    private courseService: CourseService,
    private messageService: MessageService,
    private router: Router,
    private http: HttpClient,
    private fb: FormBuilder,
    private authService: AuthService
  ) {
    this.textForm = this.fb.group({
      submission: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      this.assignmentId = params.get('id');
      this.userId = this.authService.getUserIdFromCookie();
      if (this.assignmentId) {
        this.loadAssignmentData();
      }
    });
  }

  loadAssignmentData() {
    if (!this.assignmentId) return;

    this.loading = true;
    // First load the assignment data
    this.courseService.getAssignmentById(this.assignmentId).subscribe({
      next: (result) => {
        if (result.status === 1) {
          this.assignment = result.data;
          // After assignment is loaded, get the submission data
          this.loadSubmissionData();
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: result.message || 'Failed to load assignment data',
            life: 3000
          });
          this.loading = false;
        }
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load assignment data',
          life: 3000
        });
        this.loading = false;
      }
    });
  }

  loadSubmissionData() {
    if (!this.assignmentId) return;

    this.courseService.getAssignmentSubmissionByUserIdAndAssignmentId(this.assignmentId, this.userId).subscribe({
      next: (response) => {
        if (response && response.status === 1) {
          this.submission = response.data;

          // If there's an existing text submission, populate the form
          if (this.submission && this.assignment?.assignmentType === 'TEXT') {
            this.textForm.get('submission')?.setValue(this.submission.data1);
          }
          if(this.submission && this.assignment?.assignmentType === 'FILE'){
            if (this.submission.data1) {
              const fileNames = this.submission.data1.split(';');
              this.uploadedFileInfo = fileNames.map((name, index) => ({
                originalName: name,
                index: index,
                submissionId: this.submission?.id
              }));
            }
          }
        }
        this.loading = false;
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load submission data',
          life: 3000
        });
        this.loading = false;
      }
    });
  }

  submitTextAssignment() {
    if (!this.assignmentId || !this.textForm.valid) return;

    this.submitting = true;
    const submission: AssignmentSubmission = {
      assignmentId: Number(this.assignmentId),
      data1: this.textForm.get('submission')?.value,
      userId: this.userId
    };

    this.courseService.addAssignmentSubmissionText(submission)
      .pipe(finalize(() => this.submitting = false))
      .subscribe({
        next: (response) => {
          if (response && response.status === 1) {
            this.messageService.add({
              severity: 'success',
              summary: 'Success',
              detail: 'Assignment submitted successfully',
              life: 3000
            });
            this.loadSubmissionData();
          } else {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: response.message || 'Failed to submit assignment',
              life: 3000
            });
          }
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error.error?.message || 'Failed to submit assignment',
            life: 3000
          });
        }
      });
  }

  downloadFile(submissionId: number | undefined, fileIndex: number): void {
    if (!submissionId) return;

    // Show loading indicator
    this.loading = true;

    this.courseService.downloadSubmissionFile(submissionId, fileIndex).subscribe({
      next: (blob) => {
        const fileName = this.uploadedFileInfo.find(f => f.index === fileIndex)?.originalName || `file-${fileIndex}`;

        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName; // Set filename for download

        document.body.appendChild(a);
        a.click();

        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        this.loading = false;
      },
      error: (error) => {
        console.error('Download failed:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to download the file',
          life: 3000
        });
        this.loading = false;
      }
    });
  }

  onFileSelect(event: any) {
    this.uploadedFiles = event.files;
  }

  submitFileAssignment() {
    if (!this.assignmentId || this.uploadedFiles.length === 0) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Warning',
        detail: 'Please select at least one file to upload',
        life: 3000
      });
      return;
    }

    this.submitting = true;
    const formData = new FormData();
    formData.append('assignmentId', this.assignmentId);


    for (let file of this.uploadedFiles) {
      formData.append('files', file);
    }

    this.courseService.addAssignmentSubmissionFiles(formData)
      .pipe(finalize(() => this.submitting = false))
      .subscribe({
        next: (response) => {
          if (response && response.status === 1) {
            this.messageService.add({
              severity: 'success',
              summary: 'Success',
              detail: 'Files uploaded successfully',
              life: 3000
            });
            this.uploadedFiles = [];
            this.loadSubmissionData();
          } else {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: response.message || 'Failed to upload files',
              life: 3000
            });
          }
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error.error?.message || 'Failed to upload files',
            life: 3000
          });
        }
      });
  }

  getSubmissionStatus() {
    if (!this.assignment) return 'Not Available';

    const now = new Date();
    const startDate = new Date(this.assignment.startDate);
    const endDate = new Date(this.assignment.endDate);

    if (now < startDate) {
      return 'Not Started';
    } else if (now > endDate) {
      return this.submission ? 'Submitted (Past Due Date)' : 'Overdue';
    } else {
      return this.submission ? 'Submitted' : 'Open';
    }
  }

  getStatusSeverity() {
    const status = this.getSubmissionStatus();
    switch (status) {
      case 'Submitted':
      case 'Submitted (Past Due Date)':
        return 'success';
      case 'Open':
        return 'info';
      case 'Not Started':
        return 'warning';
      case 'Overdue':
        return 'danger';
      default:
        return 'info';
    }
  }

  canSubmit() {
    if (!this.assignment) return false;

    const now = new Date();
    const startDate = new Date(this.assignment.startDate);
    const endDate = new Date(this.assignment.endDate);

    // Check if assignment is active
    if (now < startDate) return false;

    // Check if past due date and prevent late submissions
    if (now > endDate && this.assignment.preventLate) return false;

    // Check if already submitted and resubmit not allowed
    if (this.submission && !this.assignment.resubmit) return false;

    return true;
  }

  goBack() {
    this.router.navigate(['/student/course-detail/' + this.assignment?.courseId]);
  }

  formatDate(date: any): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleString();
  }

  getSubmissionWarningMessage(): string {
    if (!this.assignment) return 'You cannot submit this assignment.';

    const now = new Date();
    const startDate = new Date(this.assignment.startDate);
    const endDate = new Date(this.assignment.endDate);

    if (now < startDate) {
      return 'This assignment is not available for submission yet.';
    } else if (now > endDate && this.assignment.preventLate) {
      return 'Submission deadline has passed and late submissions are not allowed.';
    } else {
      return 'You cannot submit this assignment.';
    }
  }
}
