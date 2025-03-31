// auth.service.ts
import { Injectable } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { KeycloakProfile } from 'keycloak-js';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private keycloakService: KeycloakService) {}

  async getUserProfile(): Promise<KeycloakProfile> {
    return await this.keycloakService.loadUserProfile();
  }

  getToken(): Promise<string> {
    return this.keycloakService.getToken();
  }

  getUserRoles(): string[]{
    return this.keycloakService.getUserRoles();
  }

  hasRole(role: string): boolean {
    return this.keycloakService.getUserRoles().includes(role);
  }

  logout() {
    this.keycloakService.logout();
  }

  // Lưu thông tin vào localStorage
  saveUserToLocalStorage(profile: KeycloakProfile) {
    localStorage.setItem('user_profile', JSON.stringify(profile));
  }

  saveRoleToLocalStorage(role: string) {
    localStorage.setItem('role', role);
  }

  // Lấy thông tin từ localStorage
  getUserFromLocalStorage(): KeycloakProfile | null {
    const user = localStorage.getItem('user_profile');
    return user ? JSON.parse(user) : null;
  }

  async getTokenDetails(): Promise<any> {
    const token = await this.keycloakService.getToken();
    return this.keycloakService.getKeycloakInstance().tokenParsed;
  }

  // Lưu thông tin vào cookie
  saveUserToCookie(profile: KeycloakProfile) {
    let userId = profile.id;
    let userName = profile.username;
    document.cookie = `userId=${userId}; path=/; max-age=86400`; // Expires after 1 day
    document.cookie = `userName=${userName}; path=/; max-age=86400`; // Expires after 1 day
    document.cookie = `user_profile=${JSON.stringify(profile)}; path=/; max-age=86400`; // Hết hạn sau 1 ngày
  }

  getUserIdFromCookie(): string | null {
    const cookie = document.cookie.split('; ').find(row => row.startsWith('userId='));
    return cookie ? cookie.split('=')[1] : null;
  }

  // Lấy thông tin từ cookie
  getUserFromCookie(): KeycloakProfile | null {
    const cookie = document.cookie.split('; ').find(row => row.startsWith('user_profile='));
    return cookie ? JSON.parse(cookie.split('=')[1]) : null;
  }
}
