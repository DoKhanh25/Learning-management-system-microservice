import {Component, OnInit} from '@angular/core';
import {ConfirmationService, MenuItem, MessageService} from "primeng/api";
import {CourseService} from "../../../services/course/course.service";
import {Course} from "../../../../model/course";
import {Router} from "@angular/router";

@Component({
  selector: 'app-course-list',
  templateUrl: './course-list.component.html',
  styleUrl: './course-list.component.css'
})
export class CourseListComponent implements OnInit{
  items: MenuItem[] | undefined;
  loading: boolean = true;
  selectedCourses!: any;
  searchQuery!:string;

  first = 0;
  rows = 10;

  courses!: Course[]

  constructor(private courseService: CourseService,
              private messageService: MessageService,
              private router: Router,
              private confirmationService: ConfirmationService) {
  }
  ngOnInit() {
    this.items =
      [
        { icon: 'pi pi-home', route: '/' },
        { label: 'Quản trị khóa học'}
      ];

    this.courseService.getAllCourses().subscribe((result) => {
      if(result.status == 1){
        this.courses = result.data;
      }
      this.loading = false;

    }, (error) => {
      if(error){
        this.loading = false;
        this.messageService.add(
          { severity: 'error',
            summary: 'Warn',
            detail: 'Lỗi server',
            life: 2000
          });
      }})

  }

  clickAddCourse(){
    this.router.navigate(['admin/course-add'])
  }

  clickCourseDetail(courseId: any){
    this.router.navigate(['admin/course-detail', courseId]);
  }


  pageChange(event: any) {
    this.first = event.first;
    this.rows = event.rows;
  }

  clickDeleteCourse(id: any) {
    this.confirmationService.confirm({
      message: 'Bạn có chắc chắn muốn xóa khóa học này?',
      header: 'Xác nhận xóa',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.courseService.deleteCourseById(id).subscribe((response) => {
          if (response.status === 1) {
            this.messageService.add({
              severity: 'success',
              summary: 'Xóa thành công',
              detail: 'Khóa học đã được xóa.',
              life: 2000
            });
            this.courses = [];
            this.selectedCourses = null;
            this.ngOnInit();
          } else {
            this.messageService.add({
              severity: 'error',
              summary: 'Xóa thất bại',
              detail: 'Không thể xóa khóa học.',
              life: 2000
            });
          }
        }, (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Đã xảy ra lỗi khi xóa khóa học.',
            life: 2000
          });
        });
      }
    });
  }


}
