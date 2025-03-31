import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ConfirmationService, MenuItem, MessageService} from 'primeng/api';
import { CourseService } from '../../../services/course/course.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DialogService } from 'primeng/dynamicdialog';
import {CourseSection} from "../../../../model/course";
import {Assignment} from "../../../../model/assignment";

@Component({
  selector: 'app-course-detail',
  templateUrl: './course-detail.component.html',
  styleUrl: './course-detail.component.css',
  providers: [DialogService, ConfirmationService]
})
export class CourseDetailComponent implements OnInit {
  courseId: string | null = null;
  course: any = null;
  sections: any[] = [];
  loading = false;
  items: MenuItem[] = [];
  showSectionForm = false;
  showLessonForm = false;
  currentSectionId: number | null = null;

  sectionForm: FormGroup;
  lessonForm: FormGroup;
  expandedSections: {[key: number]: boolean} = {};

  assignments: Assignment[] = [];
  assignmentForm: FormGroup;
  showAssignmentForm = false;

  isEditMode = false;
  currentAssignment: Assignment | null = null;

  isSectionEditMode = false;
  currentSection: CourseSection | null = null;


  constructor(
    private route: ActivatedRoute,
    private courseService: CourseService,
    private messageService: MessageService,
    private fb: FormBuilder,
    private dialogService: DialogService,
    private router: Router,
    private confirmationService: ConfirmationService

  ) {

    this.sectionForm = this.fb.group({
      name: ['', Validators.required],
      summary: ['']
    });

    this.lessonForm = this.fb.group({
      name: ['', Validators.required],
      intro: ['']
    });

    this.assignmentForm = this.fb.group({
      name: ['', Validators.required],
      description: ['', Validators.required],
      startDate: [null, Validators.required],
      endDate: [null, Validators.required],
      assignmentType: ['TEXT'],
      resubmit: [false],
      preventLate: [false]
    });



  }

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
      { icon: 'pi pi-home', routerLink: '/home' },
      { label: 'My Courses', routerLink: '/courses' },
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

  // Add method to load assignments
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

  openAssignmentForm() {
    this.isEditMode = false;
    this.currentAssignment = null;
    this.assignmentForm.reset({
      assignmentType: 'TEXT',
      resubmit: false,
      preventLate: false
    });
    this.assignmentForm.get('name')?.enable();
    this.assignmentForm.get('assignmentType')?.enable();
    this.showAssignmentForm = true;
  }


  openSectionFormForEdit(section: any) {
    this.isSectionEditMode = true;
    this.currentSection = section;
    this.sectionForm.patchValue({
      name: section.name,
      summary: section.summary
    });
    this.sectionForm.get('name')?.disable();
    this.showSectionForm = true;
  }


  onAssignmentSubmit() {
    if (this.assignmentForm.invalid || !this.courseId) {
      this.messageService.add({
        severity: 'error',
        summary: 'Validation Error',
        detail: 'Please check all required fields',
        life: 3000
      });
      return;
    }

    const formValues = this.assignmentForm.getRawValue(); // Gets all values including disabled fields
    const assignmentData: Assignment = {
      ...formValues,
      courseId: Number(this.courseId)
    };

    // If in edit mode, add the ID and call update
    if (this.isEditMode && this.currentAssignment) {
      assignmentData.id = this.currentAssignment.id;
      this.courseService.updateAssignment(assignmentData).subscribe({
        next: (response) => {
          if (response.status === 1) {
            this.messageService.add({
              severity: 'success',
              summary: 'Success',
              detail: 'Assignment updated successfully',
              life: 3000
            });
            this.showAssignmentForm = false;
            this.loadAssignments();
          }
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to update assignment',
            life: 3000
          });
        }
      });
    } else {
      // Create new assignment
      this.courseService.addAssignment(assignmentData).subscribe({
        next: (response) => {
          if (response.status === 1) {
            this.messageService.add({
              severity: 'success',
              summary: 'Success',
              detail: 'Assignment created successfully',
              life: 3000
            });
            this.showAssignmentForm = false;
            this.loadAssignments();
          }
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to create assignment',
            life: 3000
          });
        }
      });
    }
  }


  openAssignmentFormForEdit(assignment: Assignment) {
    this.isEditMode = true;
    this.currentAssignment = assignment;
    const assignmentWithParsedDates = {
      ...assignment,
      startDate: assignment.startDate ? new Date(assignment.startDate) : null,
      endDate: assignment.endDate ? new Date(assignment.endDate) : null
    };

    this.assignmentForm.patchValue(assignmentWithParsedDates);
    this.assignmentForm.get('name')?.disable();
    this.assignmentForm.get('assignmentType')?.disable();
    this.showAssignmentForm = true;
    console.log(this.assignmentForm.value)
  }

  deleteAssignment(id: number) {
    this.confirmationService.confirm({
      message: 'Are you sure you want to delete this assignment?',
      accept: () => {
        this.courseService.deleteAssignmentById(id).subscribe({
          next: (response) => {
            if (response.status === 1) {
              this.messageService.add({
                severity: 'success',
                summary: 'Success',
                detail: 'Assignment deleted successfully',
                life: 3000
              });
              this.loadAssignments();
            }
          },
          error: (error) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Failed to delete assignment',
              life: 3000
            });
          }
        });
      }
    });
  }

  toggleSection(sectionId: number) {
    this.expandedSections[sectionId] = !this.expandedSections[sectionId];
  }

  openSectionForm() {
    this.isSectionEditMode = false;
    this.currentSection = null;
    this.sectionForm.reset();
    this.sectionForm.get('name')?.enable();
    this.showSectionForm = true;
  }

  openLessonForm(sectionId: number) {
    this.currentSectionId = sectionId;
    this.showLessonForm = true;
    this.lessonForm.reset();
  }

  onSectionSubmit() {
    if (this.sectionForm.invalid || !this.courseId) {
      this.messageService.add({
        severity: 'error',
        summary: 'Validation Error',
        detail: 'Please check all required fields',
        life: 3000
      });
      return;
    }

    // Get all form values including disabled fields
    const formValues = this.sectionForm.getRawValue();

    if (this.isSectionEditMode && this.currentSection) {
      // Update existing section
      const sectionData: CourseSection = {
        ...this.currentSection,
        summary: formValues.summary
      };

      this.courseService.updateCourseSection(sectionData).subscribe({
        next: (response) => {
          if (response.status === 1) {
            this.messageService.add({
              severity: 'success',
              summary: 'Success',
              detail: 'Section updated successfully',
              life: 3000
            });
            this.showSectionForm = false;
            this.loadCourseData();
          }
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to update section',
            life: 3000
          });
        }
      });
    } else {
      // Create new section
      const sectionData: CourseSection = {
        ...formValues,
        courseId: this.courseId,
        section: this.sections.length + 1 // Auto-number the section
      };

      this.courseService.addCourseSection(sectionData).subscribe({
        next: (response) => {
          if (response.status === 1) {
            this.messageService.add({
              severity: 'success',
              summary: 'Success',
              detail: 'Section created successfully',
              life: 3000
            });
            this.showSectionForm = false;
            this.loadCourseData();
          }
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to create section',
            life: 3000
          });
        }
      });
    }
  }

  onLessonSubmit() {
    if (this.lessonForm.invalid || !this.currentSectionId) {
      this.messageService.add({
        severity: 'error',
        summary: 'Validation Error',
        detail: 'Please check all required fields',
        life: 3000
      });
      return;
    }

    const lessonData = {
      ...this.lessonForm.value,
      sectionId: this.currentSectionId
    };

    this.courseService.addLesson(lessonData).subscribe({
      next: (response) => {
        if (response.status === 1) {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Lesson created successfully',
            life: 3000
          });
          this.showLessonForm = false;
          this.loadCourseData();
        }
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to create lesson',
          life: 3000
        });
      }
    });
  }

  clickDeleteCourseSection(id: any) {
    this.confirmationService.confirm({
      message: 'Are you sure you want to delete this section?',
      accept: () => {
        this.courseService.deleteCourseSectionById(id).subscribe({
          next: (response) => {
            if (response.status === 1) {
              this.messageService.add({
                severity: 'success',
                summary: 'Success',
                detail: 'Section deleted successfully',
                life: 3000
              });
              this.loadCourseData();
            }
          },
          error: (error) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Failed to delete section',
              life: 3000
            });
          }
        });
      }
    });
  }
  clickNavigateLessonDetail(lessonId: any){
    this.router.navigate(['/user/lesson-detail', lessonId]);
  }

  clickNavigateLessonManagement(lessonId: any){
    this.router.navigate(['/user/lesson-management', lessonId]);
  }
  clickNavigateAssignment(assignmentId: any){
    this.router.navigate(['/user/assignment-management', assignmentId]);

  }
}
