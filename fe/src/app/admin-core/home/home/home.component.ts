import { Component, OnInit } from '@angular/core';
import { MenuItem } from "primeng/api";
import { Router } from "@angular/router";
import {CourseService} from "../../../services/course/course.service";
import {UserService} from "../../../services/user-service/user.service";
import {AuthService} from "../../../services/auth/auth.service";
import {CohortService} from "../../../services/cohort/cohort.service";
import {DiscoveryService} from "../../../services/discovery/discovery.service";
import {QuizService} from "../../../services/quiz/quiz.service";

interface Statistic {
  label: string;
  value: number | string;
  icon: string;
  cardClass: string;
  progressValue: number;
}

interface QuickAccess {
  title: string;
  description: string;
  icon: string;
  route: string;
}

interface Activity {
  description: string;
  time: Date;
  icon: string;
  color: string;
}

interface SystemStatus {
  label: string;
  value: number;
  class: string;
}

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  items: MenuItem[] | undefined;
  adminName: string = 'Admin';
  currentDate: Date = new Date();

  statistics: Statistic[] = [];
  quickAccessMenu: QuickAccess[] = [];
  recentActivities: Activity[] = [];
  systemStatus: SystemStatus[] = [];
  microservices: any[] = [];
  serviceStatusLoading: boolean = true;

  selectedService: any = null;
  serviceDetailsVisible: boolean = false;


  constructor(private router: Router,
              private courseService: CourseService,
              private userService: UserService,
              private authService: AuthService,
              private cohortService: CohortService,
              private discoveryService: DiscoveryService,
              private quizService: QuizService) {}

  ngOnInit(): void {
    this.items = [
      { label: '', icon: 'pi pi-home', route: '/admin/home' }
    ];

    this.adminName = this.authService.getUserFromCookie()?.username || "Admin";

    this.loadMicroservices();
    this.loadStatistics();
    this.setupQuickAccessMenu();
    this.loadRecentActivities();
    this.loadSystemStatus();
  }

  navigateTo(route: string): void {
    if (route.startsWith('http')) {
      window.open(route, '_blank');
    } else {
      this.router.navigate([route]);
    }
  }


  showServiceDetails(service: any): void {
    this.selectedService = service;
    this.serviceDetailsVisible = true;
  }

  private loadMicroservices(): void {
    this.serviceStatusLoading = true;
    this.discoveryService.getAllServices().subscribe({
      next: (services) => {
        this.microservices = services.map((service: any) => {
          // Format the service data
          return {
            name: service.name,
            instances: service.instance ?
              (Array.isArray(service.instance) ? service.instance : [service.instance]) : [],
            status: this.getServiceStatus(service)
          };
        });
        this.serviceStatusLoading = false;
      },
      error: (error) => {
        console.error('Error loading services:', error);
        this.serviceStatusLoading = false;
        this.microservices = [];
      }
    });
  }

  private getServiceStatus(service: any): string {
    if (!service.instance) return 'DOWN';

    const instances = Array.isArray(service.instance) ? service.instance : [service.instance];

    // If any instance is up, consider the service as up
    const anyUp = instances.some((instance: any) =>
      instance.status && instance.status.toLowerCase() === 'up');

    return anyUp ? 'UP' : 'DOWN';
  }


  getStatusColor(status: string): string {
    return status === 'UP' ? 'green' : 'red';
  }

  // Convert status to friendly badge class
  getStatusBadge(status: string): string {
    return status === 'UP' ? 'p-badge-success' : 'p-badge-danger';
  }

  // Refresh services data
  refreshServices(): void {
    this.loadMicroservices();
  }

  private loadStatistics(): void {
    // Initialize with loading state
    this.statistics = [
      { label: 'Người dùng', value: '...', icon: 'pi pi-users', cardClass: 'user-card', progressValue: 0 },
      { label: 'Khóa học', value: '...', icon: 'pi pi-book', cardClass: 'course-card', progressValue: 0 },
      { label: 'Nhóm', value: '...', icon: 'pi pi-sitemap', cardClass: 'cohort-card', progressValue: 0 },
      { label: 'Bài kiểm tra', value: '...', icon: 'pi pi-file-edit', cardClass: 'exam-card', progressValue: 0 }
    ];

    // Fetch real user count
    this.userService.getAllUsers().subscribe(
      (result) => {
        if (result && result.status === 1) {
          this.statistics[0].value = result.data.length || 0;
          this.statistics[0].progressValue = Math.min(result.data.totalUsers / 2, 100);
        }
      }
    );

    // Fetch real course count
    this.courseService.getAllCourses().subscribe(
      (result) => {
        if (result && result.status === 1) {
          this.statistics[1].value = result.data.length || 0;
        }
      }
    );



    // Assuming you have a cohort service with a method to get stats
    this.cohortService.getAllCohorts().subscribe(
      (result) => {
        if (result && result.status === 1) {
          const cohortCount = result.data ? result.data.length : 0;
          this.statistics[2].value = cohortCount;
        }
      }
    );

    this.quizService.countAllExams().subscribe((result) => {
      if(result && result.status == 1){
        this.statistics[3].value = result.data;
      }
    })
  }

  private setupQuickAccessMenu(): void {
    this.quickAccessMenu = [
      {
        title: 'Tài khoản',
        description: 'Quản lý người dùng',
        icon: 'pi pi-user',
        route: '/admin/account-management'
      },
      {
        title: 'Khóa học',
        description: 'Quản lý khóa học',
        icon: 'pi pi-book',
        route: '/admin/course-management'
      },
      {
        title: 'Nhóm',
        description: 'Quản lý nhóm',
        icon: 'pi pi-users',
        route: '/admin/cohort-management'
      },
      {
        title: 'Phân quyền',
        description: 'Quản lý vai trò và quyền',
        icon: 'pi pi-key',
        route: '/admin/role-management'
      },
      {
        title: 'Chính sách',
        description: 'Quản lý chính sách',
        icon: 'pi pi-shield',
        route: '/admin/policy-management'
      },
      {
        title: 'Keycloak',
        description: 'Truy cập Keycloak',
        icon: 'pi pi-lock',
        route: 'http://localhost:8080'
      }
    ];
  }

  private loadRecentActivities(): void {
    // In a real application, these would be fetched from a service
    this.recentActivities = [
      {
        description: 'Thêm khóa học "Lập trình Java cơ bản"',
        time: new Date(Date.now() - 1000 * 60 * 30), // 30 minutes ago
        icon: 'pi pi-plus',
        color: '#3B82F6'
      },
      {
        description: 'Cập nhật tài khoản "admin@example.com"',
        time: new Date(Date.now() - 1000 * 60 * 120), // 2 hours ago
        icon: 'pi pi-user-edit',
        color: '#10B981'
      },
      {
        description: 'Xóa nhóm "Nhóm học lập trình web"',
        time: new Date(Date.now() - 1000 * 60 * 240), // 4 hours ago
        icon: 'pi pi-trash',
        color: '#EF4444'
      },
      {
        description: 'Tạo bài kiểm tra "Kiểm tra giữa kỳ"',
        time: new Date(Date.now() - 1000 * 60 * 60 * 24), // 1 day ago
        icon: 'pi pi-file-edit',
        color: '#8B5CF6'
      }
    ];
  }

  private loadSystemStatus(): void {
    this.systemStatus = [
      { label: 'CPU Usage', value: 45, class: 'bg-primary' },
      { label: 'Memory Usage', value: 78, class: 'bg-warning' },
      { label: 'Disk Space', value: 32, class: 'bg-success' },
      { label: 'Network', value: 68, class: 'bg-info' }
    ];
  }
}
