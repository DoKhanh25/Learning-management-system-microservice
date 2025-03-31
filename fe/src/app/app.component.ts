import {Component, CUSTOM_ELEMENTS_SCHEMA, OnInit} from '@angular/core';
import {KeycloakProfile} from "keycloak-js";
import {AuthService} from "./services/auth/auth.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit{
  title = 'App';
  userProfile: KeycloakProfile | null = null;

  constructor(private authService: AuthService) {}
  async ngOnInit() {
    try {
      const profile = await this.authService.getUserProfile();
      this.authService.saveUserToCookie(profile);
    } catch (error) {
      console.error('Error loading user profile:');
    }
  }
}
