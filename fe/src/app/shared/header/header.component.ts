import { Component, OnInit, ViewChild } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { Sidebar } from 'primeng/sidebar';
import {KeycloakService} from "keycloak-angular";
import {Router} from "@angular/router";
import {AuthService} from "../../services/auth/auth.service";
import {ChatService} from "../../services/chat/chat.service";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit {
  items: MenuItem[] | undefined;
  logoPath = 'assets/introduction/logo.png';
  role!: string | null;
  username!: string | null;


  constructor(private keycloakService: KeycloakService,
              private router: Router,
              private authService: AuthService,
              private chatService: ChatService) {
  }

  @ViewChild('sidebarRef') sidebarRef!: Sidebar;

    closeCallback(e: any): void {
        this.sidebarRef.close(e);
    }

    sidebarVisible: boolean = false;


  ngOnInit(): void {
    this.items = [
      {
        label: 'Cập nhật thông tin',
        icon: 'pi pi-user-edit',
        routerLink: '/user/update-profile'
      },
      {
        label: 'Đăng xuất',
        icon: 'pi pi-power-off',
        command: event => this.logout()
      }
    ]


    this.loadProfile();
  }

  toggleChat(): void {
    this.chatService.toggleChatVisibility();
  }



  loadProfile(){
    let roles = this.authService.getUserRoles();
    if(roles.includes('ROLE_ADMIN')){
      this.role = 'ADMIN';
    }
    else if(roles.includes('ROLE_TEACHER') && !roles.includes('ROLE_ADMIN')){
      this.role = 'TEACHER';
    }
    else if(roles.includes('ROLE_STUDENT') && !roles.includes('ROLE_ADMIN') && !roles.includes('ROLE_TEACHER')){
      this.role = 'USER';
    }

    this.authService.getUserProfile().then(profile => {
      this.username = profile.username || null;
    })
  }

  async logout(): Promise<void> {
    try {
      localStorage.removeItem('user_profile'); // Xóa localStorage
      localStorage.removeItem('role')
      document.cookie = 'user_profile=; max-age=0'; // Xóa cookie
      await this.keycloakService.logout('http://localhost:4200/introduce');
    } catch (error) {
      console.error('Lỗi khi đăng xuất:', error);
    }
  }
}
