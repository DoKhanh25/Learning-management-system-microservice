import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {
  constructor(
    private router: Router,
    private keycloak: KeycloakService
  ) {}

  async canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Promise<boolean> {
    // Check if user is authenticated
    if (!this.keycloak.isLoggedIn()) {
      await this.keycloak.login({
        redirectUri: window.location.origin + state.url
      });
      return false;
    }

    // Get required roles from route data
    const requiredRoles = route.data['roles'] as string[];

    // If no roles specified, allow access
    if (!requiredRoles || requiredRoles.length === 0) {
      return true;
    }

    if (requiredRoles.some(role => this.keycloak.isUserInRole(role))) {
      return true;
    }

    this.router.navigate(['/unauthorized']);
    return false;
  }
}
