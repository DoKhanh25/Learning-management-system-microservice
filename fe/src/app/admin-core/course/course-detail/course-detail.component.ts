import { Component, OnInit } from '@angular/core';
import { ConfirmationService, MenuItem, MessageService } from "primeng/api";
import { FormBuilder, FormControl, FormGroup, Validators } from "@angular/forms";
import { UserService } from "../../../services/user-service/user.service";
import { CohortService } from "../../../services/cohort/cohort.service";
import { User } from "../../../../model/user";
import { ActivatedRoute, Router } from "@angular/router";
import { CourseService } from "../../../services/course/course.service";
import { Result } from "../../../../model/result";
import { Enrolment } from '../../../../model/course';
import { Cohort } from '../../../../model/cohort';
import { finalize } from 'rxjs/operators';

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
export class CourseDetailComponent implements OnInit {
  items: MenuItem[] | undefined;
  courseUpdateForm!: FormGroup;
  enrolKeyUpdateForm!: FormGroup;
  cohortAddEnrolForm!: FormGroup;
  studentAddForm!: FormGroup;

  courseId!: string | null;
  loading = false;
  saving = false;
  addingStudent = false;

  cohorts: Cohort[] = [];
  sourceUsers: User[] = [];
  targetUsers: User[] = [];
  availableStudents: any[] = [];
  usersInCourse: any[] = [];
  selectedUsers: any[] = [];
  enrolments: Enrolment[] = [];

  searchQuery: string = '';
  addEnrolmentDialog = false;
  first = 0;
  rows = 10;

  courseRoles = [
    { name: 'Tạo mã cho giáo viên', value: "TEACHER" },
    { name: 'Tạo mã cho sinh viên', value: "STUDENT" }
  ];

  courseRolesCohort = [
    { name: 'Giáo viên', value: 'TEACHER' },
    { name: 'Học viên', value: 'STUDENT' }
  ];

  courseRolesStudent = [
    { name: 'Học viên', value: 'STUDENT' },
    { name: 'Giáo viên', value: 'TEACHER' }
  ];

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private messageService: MessageService,
    private route: ActivatedRoute,
    private router: Router,
    private courseService: CourseService,
    private cohortService: CohortService,
    private confirmationService: ConfirmationService
  ) {
    this.courseId = this.route.snapshot.paramMap.get('id');
  }

  ngOnInit() {
    this.initializeForms();
    this.setupBreadcrumb();
    this.loadCourseData();
  }

  private setupBreadcrumb() {
    this.items = [
      { label: 'Trang chủ', icon: 'pi pi-home', route: ['/admin'] },
      { label: 'Khóa học', route: ['/admin/course'] },
      { label: 'Chi tiết khóa học' }
    ];
  }

  private initializeForms() {
    this.courseUpdateForm = this.fb.group({
      name: ["", [Validators.required, Validators.minLength(3)]],
      summary: [""],
      startDate: new FormControl<Date | null>(null),
      endDate: new FormControl<Date | null>(null),
      showGrades: null
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

    this.studentAddForm = this.fb.group({
      userId: ['', Validators.required],
      courseRole: ['STUDENT', Validators.required]
    });
  }

  loadCourseData() {
    if (!this.courseId) {
      this.messageService.add({
        severity: 'error',
        summary: 'Lỗi',
        detail: 'Không tìm thấy khóa học',
        life: 3000
      });
      this.router.navigate(['/admin/course']);
      return;
    }

    this.loading = true;

    // Load course data
    this.courseService.getCourseById(this.courseId)
      .pipe(finalize(() => {
        // After course data loads, get other data
        this.fetchUserEnrolData(this.courseId);
        this.fetchEnrolments();
        this.fetchCohorts();
      }))
      .subscribe({
        next: (result: Result) => {
          if (result.status === 1) {
            const course = result.data;
            this.courseUpdateForm.patchValue({
              name: course.name,
              summary: course.summary,
              startDate: new Date(course.startDate),
              endDate: new Date(course.endDate)
            });
          } else {
            this.messageService.add({
              severity: 'error',
              summary: 'Lỗi',
              detail: 'Không tìm thấy thông tin khóa học',
              life: 3000
            });
          }
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Lỗi kết nối',
            detail: 'Không thể tải thông tin khóa học',
            life: 3000
          });
          this.loading = false;
        }
      });
  }

  private fetchEnrolments() {
    if (!this.courseId) return;

    this.courseService.getEnrolByCourseAndEnrolTypeManual(this.courseId, "SELF").subscribe({
      next: (result) => {
        if (result.status === 1) {
          this.enrolments = result.data;
        }
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Lỗi',
          detail: 'Không thể tải thông tin mã tham gia',
          life: 3000
        });
      }
    });
  }

  private fetchCohorts() {
    this.cohortService.getAllCohorts().subscribe({
      next: (result: Result) => {
        if (result.status === 1) {
          this.cohorts = result.data;
        }
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Lỗi',
          detail: 'Không thể tải danh sách nhóm',
          life: 3000
        });
      }
    });
  }

  fetchUserEnrolData(courseId: any) {
    if (!courseId) return;

    this.loading = true;

    this.courseService.getAllUserEnrolmentsByCourseId(courseId).subscribe({
      next: (result: Result) => {
        if (result.status === 1) {
          const userEnrolments: UserEnrolment[] = result.data;
          const userIds = userEnrolments.map(enrol => enrol.userId);

          if (userIds.length > 0) {
            this.userService.getUsersByIds(userIds).subscribe({
              next: (userResult: Result) => {
                if (userResult.status === 1) {
                  // Combine user data with enrollment data
                  this.usersInCourse = userResult.data.map((user: any) => {
                    const enrolment: any = userEnrolments.find(e => e.userId === user.userId);
                    return {
                      ...user,
                      courseRole: enrolment?.enrol?.courseRole
                    };
                  });
                }
                this.loadAvailableStudents();
                this.loading = false;
              },
              error: () => {
                this.loading = false;
                this.messageService.add({
                  severity: 'error',
                  summary: 'Lỗi',
                  detail: 'Không thể tải thông tin người dùng',
                  life: 3000
                });
              }
            });
          } else {
            this.usersInCourse = [];
            this.loadAvailableStudents();
            this.loading = false;
          }
        } else {
          this.loading = false;
          this.messageService.add({
            severity: 'warn',
            summary: 'Thông báo',
            detail: 'Không có người dùng nào trong khóa học này',
            life: 3000
          });
        }
      },
      error: () => {
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Lỗi',
          detail: 'Không thể tải danh sách người tham gia',
          life: 3000
        });
      }
    });
  }

  loadAvailableStudents() {
    this.userService.getAllUsers().subscribe({
      next: (result: Result) => {
        if (result.status === 1) {
          // Get IDs of users already enrolled in the course
          const enrolledUserIds = this.usersInCourse.map(user => user.userId);

          // Filter out users already enrolled
          this.availableStudents = result.data
            .filter((user: any) => !enrolledUserIds.includes(user.userId))
            .map((user: any) => ({
              ...user,
              fullName: `${user.lastName} ${user.firstName}`
            }));

          console.log(this.availableStudents)
        } else {
          this.messageService.add({
            severity: 'warn',
            summary: 'Thông báo',
            detail: 'Không có người dùng nào để thêm vào khóa học',
            life: 3000
          });
        }
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Lỗi',
          detail: 'Không thể tải danh sách người dùng',
          life: 3000
        });
      }
    });
  }

  onUpdateCourse() {
    if (this.courseUpdateForm.invalid) {
      this.messageService.add({
        severity: 'error',
        summary: 'Lỗi dữ liệu',
        detail: 'Vui lòng điền đầy đủ thông tin bắt buộc',
        life: 3000
      });
      return;
    }

    this.saving = true;
    const courseData = this.courseUpdateForm.value;

    this.courseService.updateCourse(this.courseId, courseData)
      .pipe(finalize(() => this.saving = false))
      .subscribe({
        next: (result: Result) => {
          if (result.status === 1) {
            this.messageService.add({
              severity: 'success',
              summary: 'Thành công',
              detail: 'Đã cập nhật thông tin khóa học',
              life: 3000
            });
          } else {
            this.messageService.add({
              severity: 'error',
              summary: 'Lỗi',
              detail: result.message || 'Không thể cập nhật khóa học',
              life: 3000
            });
          }
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Lỗi kết nối',
            detail: 'Không thể cập nhật khóa học',
            life: 3000
          });
        }
      });
  }

  onUpdateEnrolment() {
    if (this.enrolKeyUpdateForm.invalid) {
      this.messageService.add({
        severity: 'error',
        summary: 'Lỗi dữ liệu',
        detail: 'Vui lòng điền đầy đủ thông tin mã tham gia',
        life: 3000
      });
      return;
    }

    this.saving = true;
    const enrolData = this.enrolKeyUpdateForm.value;

    this.courseService.createSelfEnrol(enrolData)
      .pipe(finalize(() => {
        this.saving = false;
        this.addEnrolmentDialog = false;
      }))
      .subscribe({
        next: (result: Result) => {
          if (result.status === 1) {
            this.messageService.add({
              severity: 'success',
              summary: 'Thành công',
              detail: 'Đã tạo mã tham gia mới',
              life: 3000
            });
            this.fetchEnrolments();
            this.enrolKeyUpdateForm.reset({
              course: this.courseId,
              enrolType: 'MANUAL'
            });
          } else {
            this.messageService.add({
              severity: 'error',
              summary: 'Lỗi',
              detail: result.message || 'Không thể tạo mã tham gia',
              life: 3000
            });
          }
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Lỗi kết nối',
            detail: 'Không thể tạo mã tham gia',
            life: 3000
          });
        }
      });
  }

  onCohortAddSubmit() {
    if (this.cohortAddEnrolForm.invalid) {
      this.messageService.add({
        severity: 'error',
        summary: 'Lỗi dữ liệu',
        detail: 'Vui lòng chọn nhóm và vai trò',
        life: 3000
      });
      return;
    }

    this.saving = true;
    const formValue = this.cohortAddEnrolForm.value;

    this.courseService.addEnrolmentsByCohort(formValue.cohort, this.courseId, formValue.courseRole)
      .pipe(finalize(() => this.saving = false))
      .subscribe({
        next: (result: Result) => {
          if (result.status === 1) {
            this.messageService.add({
              severity: 'success',
              summary: 'Thành công',
              detail: 'Đã thêm nhóm vào khóa học',
              life: 3000
            });
            this.fetchUserEnrolData(this.courseId);
            this.cohortAddEnrolForm.patchValue({
              courseRole: 'STUDENT',
              cohort: ''
            });
          } else {
            this.messageService.add({
              severity: 'error',
              summary: 'Lỗi',
              detail: result.message || 'Không thể thêm nhóm vào khóa học',
              life: 3000
            });
          }
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Lỗi kết nối',
            detail: 'Không thể thêm nhóm vào khóa học',
            life: 3000
          });
        }
      });
  }

  onAddSingleStudent() {
    if (this.studentAddForm.invalid) {
      this.messageService.add({
        severity: 'error',
        summary: 'Lỗi dữ liệu',
        detail: 'Vui lòng điền đầy đủ thông tin học viên',
        life: 3000
      });
      return;
    }

    this.addingStudent = true;
    const formValue = this.studentAddForm.value;

    const enrolDTO: any = {
      course: Number(this.courseId),
      courseRole: formValue.courseRole
    };

    this.courseService.addUserEnrolment(enrolDTO, formValue.userId)
      .pipe(finalize(() => this.addingStudent = false))
      .subscribe({
        next: (response: Result) => {
          if (response.status === 1) {
            this.messageService.add({
              severity: 'success',
              summary: 'Thành công',
              detail: 'Đã thêm học viên vào khóa học',
              life: 3000
            });

            // Reset form
            this.studentAddForm.patchValue({
              userId: '',
              courseRole: 'STUDENT',
              startDate: new Date(),
              endDate: this.addMonths(new Date(), 3)
            });

            // Refresh user list
            this.fetchUserEnrolData(this.courseId);
          } else {
            this.messageService.add({
              severity: 'error',
              summary: 'Lỗi',
              detail: response.message || 'Không thể thêm học viên vào khóa học',
              life: 3000
            });
          }
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Lỗi kết nối',
            detail: 'Không thể thêm học viên vào khóa học',
            life: 3000
          });
        }
      });
  }

  removeUser(user: any) {
    this.confirmationService.confirm({
      message: `Bạn có chắc chắn muốn xóa ${user.lastName} ${user.firstName} khỏi khóa học này?`,
      header: 'Xác nhận xóa',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Xóa',
      rejectLabel: 'Hủy',
      accept: () => {
        this.loading = true;

        this.courseService.deleteUserEnrolmentsByUserIdAndCourseId(this.courseId, user.userId)
          .pipe(finalize(() => this.loading = false))
          .subscribe({
            next: (response: any) => {
              if (response.status === 1) {
                this.messageService.add({
                  severity: 'success',
                  summary: 'Thành công',
                  detail: `Đã xóa ${user.lastName} ${user.firstName} khỏi khóa học`,
                  life: 3000
                });
                this.fetchUserEnrolData(this.courseId);
              } else {
                this.messageService.add({
                  severity: 'error',
                  summary: 'Lỗi',
                  detail: response.message || 'Không thể xóa người dùng khỏi khóa học',
                  life: 3000
                });
              }
            },
            error: () => {
              this.messageService.add({
                severity: 'error',
                summary: 'Lỗi kết nối',
                detail: 'Không thể xóa người dùng khỏi khóa học',
                life: 3000
              });
            }
          });
      }
    });
  }

  pageChange(event: any) {
    this.first = event.first;
    this.rows = event.rows;
  }

  isExpired(date: Date): boolean {
    return new Date(date) < new Date();
  }

  addMonths(date: Date, months: number): Date {
    const result = new Date(date);
    result.setMonth(result.getMonth() + months);
    return result;
  }
}
