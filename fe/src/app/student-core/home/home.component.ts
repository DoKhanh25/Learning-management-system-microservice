import {Component, OnInit} from '@angular/core';
import {Course} from "../../../model/course";
import {MenuItem, MessageService} from "primeng/api";
import {CourseService} from "../../services/course/course.service";
import {AuthService} from "../../services/auth/auth.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit{
  courses: any[] = [];
  featuredCourses: Course[] = [];
  userId: string | null = null;
  items: MenuItem[] | undefined;
  teacherName: string = 'Teacher';
  totalStudents: number = 0;
  pendingAssignments: number = 0;
  upcomingEvents: number = 0;
  completionRate: number = 0;

  responsiveOptions = [
    {
      breakpoint: '1024px',
      numVisible: 3,
      numScroll: 1
    },
    {
      breakpoint: '768px',
      numVisible: 2,
      numScroll: 1
    },
    {
      breakpoint: '560px',
      numVisible: 1,
      numScroll: 1
    }
  ];

  constructor(
    private courseService: CourseService,
    private authService: AuthService,
    private messageService: MessageService,
    private router: Router
  ) {}

  ngOnInit() {
    this.fetchCourses();
    this.setupBreadcrumb();
    this.loadStudentInfo();
  }

  loadStudentInfo() {
    this.teacherName = this.authService.getUserFromCookie()?.username || "Teacher";
    this.pendingAssignments = 12;
    this.upcomingEvents = 3;
    this.completionRate = 78;
  }

  setupBreadcrumb() {
    this.items = [
      { label: 'Home', icon: 'pi pi-home', route: '/home' },
      { label: 'Dashboard', icon: 'pi pi-th-large' }
    ];
  }

  fetchCourses() {
    this.userId = this.authService.getUserIdFromCookie();
    if (!this.userId) {
      return;
    }

    this.courseService.getAllStudentCoursesByUserId().subscribe({
      next: (result) => {
        this.courses = result.data;

        // Add mock properties for visualization
        this.courses.forEach(course => {
          course.progress = this.checkCourseProgress(course) > 100 ? 100 : this.checkCourseProgress(course);
          console.log(course.progress)
          course.status = this.checkCourseStatus(course) ;
          course.studentCount = course.showGrades;
          course.length = `${Math.ceil((new Date(course.endDate).getTime() - new Date(course.startDate).getTime()) / (7 * 24 * 60 * 60 * 1000))} tuần`;
          course.image = 'assets/image/course-placeholder.jpg';
        });

        // Calculate total students
        this.totalStudents = this.courses.reduce((sum, course) => sum + (course.studentCount || 0), 0);

        // Create featured courses (select 3 with highest progress)
        this.featuredCourses = [...this.courses]
          .sort((a, b) => (b.progress || 0) - (a.progress || 0))
          .slice(0, 3);
      },
      error: (error) => {
        this.messageService.add({severity:'error', summary:'Error', detail:'Error loading courses'});
      }
    });
  }

  checkCourseStatus(course: any){
    if(new Date() > new Date(course.startDate) && new Date() < new Date(course.endDate)){
      return 'Hoạt động';
    } else if(new Date() > new Date(course.endDate)){
      return 'Hoàn thành';
    }
    return 'Sắp tới';
  }
  checkCourseProgress(course: any){
    return Math.floor(((new Date).getTime() - new Date(course.startDate).getTime())/((new Date(course.endDate).getTime() - new Date(course.startDate).getTime())/100));
  }
  navigateCourseDetail(id: any){
    this.router.navigate(['/student/course-detail', id])
  }
}
