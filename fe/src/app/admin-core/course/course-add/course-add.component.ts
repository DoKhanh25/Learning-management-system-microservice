import {ChangeDetectorRef, Component, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {UserService} from "../../../services/user-service/user.service";
import {MenuItem, MessageService} from "primeng/api";
import {CohortService} from "../../../services/cohort/cohort.service";
import {User} from "../../../../model/user";
import {CourseService} from "../../../services/course/course.service";
import {Course} from "../../../../model/course";

@Component({
  selector: 'app-course-add',
  templateUrl: './course-add.component.html',
  styleUrl: './course-add.component.css'
})
export class CourseAddComponent implements OnInit{
  items: MenuItem[] | undefined;
  courseAddForm!: FormGroup;



  constructor(private fb: FormBuilder,
              private messageService: MessageService,
              private courseService: CourseService) {
  }
  ngOnInit() {
    this.items =
        [
          { icon: 'pi pi-home', route: '/' },
          { label: 'Quản trị khóa học', url: '/admin/course-management'},
          {label: 'Thêm khóa học'}
        ];

      this.courseAddForm = this.fb.group({
        name: ["", Validators.required],
        summary: [""],
        startDate: [new FormControl<Date | null>(null), Validators.required],
        endDate: [new FormControl<Date | null>(null), Validators.required]
    })

  }
    clickAddCourse(){
      let course: Course = {
        name: this.courseAddForm.get("name")?.value,
          summary: this.courseAddForm.get("summary")?.value,
          startDate: this.courseAddForm.get("startDate")?.value,
          endDate: this.courseAddForm.get("endDate")?.value,
          showGrades: 1
      }

      this.courseService.addCourse(course).subscribe((result) => {
        if(result.status == 1){
            this.messageService.add(
                { severity: 'success',
                    summary: 'Thành công',
                    detail: 'Tạo thành công',
                    life: 2000
                });
            this.resetData();
        } else {
            this.messageService.add(
                { severity: 'warn',
                    summary: 'Không thành công',
                    detail: "Không thành công",
                    life: 2000
                });
        }
      },(err) => {
          this.messageService.add(
              { severity: 'error',
                  summary: 'Warn',
                  detail: 'Lỗi server',
                  life: 2000
              });
      })
    }

    resetData(){
      this.ngOnInit();
    }

}
