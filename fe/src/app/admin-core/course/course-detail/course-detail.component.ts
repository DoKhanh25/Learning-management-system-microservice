import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {MenuItem, MessageService} from "primeng/api";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {UserService} from "../../../services/user-service/user.service";
import {CohortService} from "../../../services/cohort/cohort.service";
import {User} from "../../../../model/user";
import {ActivatedRoute, Router} from "@angular/router";
import {CourseService} from "../../../services/course/course.service";
import {forkJoin} from "rxjs";
import {Result} from "../../../../model/result";
import { Enrolment } from '../../../../model/course';
import { Cohort } from '../../../../model/cohort';

interface UserEnrolment {
  id: number;
  status: number;
  userId: string;
  timeStart: Date;
  timeEnd: Date;
  createdTime: Date;
  updatedTime: Date;
}

@Component({
  selector: 'app-course-detail',
  templateUrl: './course-detail.component.html',
  styleUrl: './course-detail.component.css'
})
export class CourseDetailComponent implements OnInit{
  items: MenuItem[] | undefined;
  courseUpdateForm!: FormGroup;
  enrolKeyUpdateForm!: FormGroup;
  cohortAddEnrolForm!: FormGroup;

  courseId!: string | null;
  loading = true;
  saving = false;

  cohorts!: Cohort[];

  sourceUsers!: User[];
  targetUsers!: User[];

  searchQuery!: string;
  enrolments!: Enrolment[];

  usersInCourse!: any[];
  selectedUsers!: any;

  first = 0;
  rows = 10;

  addEnrolmentDialog = false;

  courseRoles = [
    { name: 'Tạo mã cho giáo viên', value: "TEACHER" },
    { name: 'Tạo mã cho sinh viên', value: "STUDENT" }
  ];

  courseRolesCohort = [
    {
    name: 'Giáo viên',
    value: 'TEACHER'
    },
   {
    name: 'Sinh viên',
    value: 'STUDENT'
  }]

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private messageService: MessageService,
    private route: ActivatedRoute,
    private router: Router,
    private courseService: CourseService,
    private cohortService: CohortService
  ) {
    this.courseId = this.route.snapshot.paramMap.get('id');
  }

  ngOnInit() {
    this.initializeForms();
    this.setupBreadcrumb();
    this.loadCourseData();
    this.fetchUserEnrolData(this.courseId);
    this.fetchEnrolments();
    this.fetchCohorts();
    this.targetUsers = [];
  }

  private fetchEnrolments(){
    this.courseService.getEnrolByCourseAndEnrolTypeManual(this.courseId, "SELF").subscribe({
      next: (result) => {
        if(result.status == 1){
          this.enrolments = result.data;
        }
      },
      error: (error) => {
        console.log(error);
      }
    })
  }
  private fetchCohorts() {
    this.cohortService.getAllCohorts().subscribe({
      next: (result: Result) => {
        if (result.status === 1) {
          this.cohorts = result.data;
        }
      },
      error: (error: any) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load cohorts',
          life: 3000
        });
      }
    });
  }

  private initializeForms() {
    this.courseUpdateForm = this.fb.group({
      name: ["", [Validators.required, Validators.minLength(3)]],
      summary: [""],
      startDate: new FormControl<Date | null>(null),
      endDate: new FormControl<Date | null>(null)
    });

    this.enrolKeyUpdateForm = this.fb.group({
      course: [this.courseId],
      name: ["", Validators.required],
      password: ["", Validators.required],
      enrolStartDate: new FormControl<Date | null>(null),
      enrolEndDate: new FormControl<Date | null>(null),
      courseRole: ["", Validators.required],
      enrolType: ["MANUAL", Validators.required]
    });

    this.cohortAddEnrolForm = this.fb.group({
      courseRole: ["STUDENT", Validators.required],
      cohort: ["", Validators.required]
    });
  }

  private setupBreadcrumb() {
    this.items = [
      { icon: 'pi pi-home', route: '/' },
      { label: 'Quản trị khóa học', url: '/admin/course-management'},
      { label: 'Cập nhật khóa học' }
    ];
  }

  private loadCourseData() {
    if (!this.courseId) return;

    this.loading = true;
    this.courseService.getCourseById(this.courseId).subscribe({
      next: (result: Result) => {
        if(result.status === 1) {
          this.courseUpdateForm.patchValue({
            name: result.data.name,
            summary: result.data.summary,
            startDate: new Date(result.data.startDate),
            endDate: new Date(result.data.endDate)
          });
        }
        this.loading = false;
      },
      error: (error: any) => {
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

  fetchUserEnrolData(courseId: string | null) {
    if (!courseId) return;

    this.loading = true;
    const allUsers = this.userService.getAllUsers();
    const userEnrolments = this.courseService.getAllUserEnrolmentsByCourseId(courseId);
    this.usersInCourse = [];

    forkJoin({
      allUsers: allUsers,
      userEnrolments: userEnrolments
    }).subscribe({
      next: ({allUsers, userEnrolments}: {allUsers: Result, userEnrolments: Result}) => {
        if(allUsers.status === 1 && userEnrolments.status === 1) {

          const enrolledUserMap = new Map();
          userEnrolments.data.forEach((enrolment: any) => {
            enrolledUserMap.set(enrolment.userId, enrolment);
          });

          this.usersInCourse = allUsers.data.filter((user: any) =>
            enrolledUserMap.has(user.userId)
          )
          .map((user: any) => {
            const enrolment = enrolledUserMap.get(user.userId);
            return {
              ...user,
              courseRole: enrolment?.enrol?.courseRole || 'STUDENT' // Default to STUDENT if role not specified
            };
          })

        }
        this.loading = false;
      },
      error: (error: any) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load user enrollment data',
          life: 3000
        });
        this.loading = false;
      }
    });
  }

  // processPickListData(enrolledUsers: UserEnrolment[], allUsers: User[]) {
  //   this.targetUsers = [];
  //   this.sourceUsers = [...allUsers];

  //   enrolledUsers.forEach(enrolled => {
  //     const userIndex = this.sourceUsers.findIndex(user => user.userId === enrolled.userId);
  //     if (userIndex !== -1) {
  //       this.targetUsers.push(this.sourceUsers[userIndex]);
  //       this.sourceUsers.splice(userIndex, 1);
  //     }
  //   });
  // }

  onUpdateCourse() {
    if (this.courseUpdateForm.invalid || !this.courseId) {
      this.messageService.add({
        severity: 'error',
        summary: 'Validation Error',
        detail: 'Please check all required fields',
        life: 3000
      });
      return;
    }

    this.saving = true;
    const courseData = this.courseUpdateForm.value;

    this.courseService.updateCourse(this.courseId, courseData).subscribe({
      next: (response: Result) => {
        if (response.status === 1) {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Course updated successfully',
            life: 3000
          });
        }
        this.saving = false;
      },
      error: (error: any) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to update course',
          life: 3000
        });
        this.saving = false;
      }
    });
  }

  onUpdateEnrolment(){
    if (this.enrolKeyUpdateForm.invalid) {
      this.messageService.add({
        severity: 'error',
        summary: 'Validation Error',
        detail: 'Please check all required fields',
        life: 3000
      });
      return;
    }

    const enrolData: Enrolment = this.enrolKeyUpdateForm.value;
    this.courseService.createSelfEnrol(enrolData).subscribe({
      next: (response: Result) => {
        if (response.status === 1) {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Enrolment keys updated successfully',
            life: 3000
          });
        }
      this.addEnrolmentDialog = false;
      this.fetchEnrolments();
      this.enrolKeyUpdateForm.reset();
      },
      error: (error: any) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to update enrolment keys',
          life: 3000
        });
      }
    });
  }

  onCohortAddSubmit() {
    if (this.cohortAddEnrolForm.invalid) {
      this.messageService.add({
        severity: 'error',
        summary: 'Validation Error',
        detail: 'Please check all required fields',
        life: 3000
      });
      return;
    }

    const cohortData = this.cohortAddEnrolForm.value;

    this.courseService.addEnrolmentsByCohort(cohortData.cohort, this.courseId, cohortData.courseRole).subscribe({
      next: (response: Result) => {
        if (response.status === 1) {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: response.message? response.message : 'Enrolment keys updated successfully',
            life: 3000
          });
          this.cohortAddEnrolForm.reset();
          this.cohortAddEnrolForm.get('courseRole')?.setValue('STUDENT');
          this.fetchUserEnrolData(this.courseId);
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: response.message? response.message : 'Failed to update enrolment keys',
            life: 3000
          });
        }
      },
      error: (error: any) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to update enrolment keys',
          life: 3000
        });
      }
    });
  }

  isExpired(date: Date | null | string) {
    if (!date) return false;
    const dateObj = date instanceof Date ? date : new Date(date);
    return dateObj.getTime() < Date.now();
  }

  pageChange(event: any) {
    this.first = event.first;
    this.rows = event.rows;
  }
}
