import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { KeycloakService } from 'keycloak-angular';
import { Observable, from } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Injectable()
export class CustomKeycloakInterceptor implements HttpInterceptor {
  constructor(private keycloakService: KeycloakService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (this.isPublicRequest(req.url)) {
      return next.handle(req);
    }

    return from(this.keycloakService.getToken()).pipe(
      switchMap((token) => {
        if (token) {
          req = req.clone({
            setHeaders: { Authorization: `Bearer ${token}` },
          });
        }
        return next.handle(req);
      })
    );
  }

  private isPublicRequest(url: string): boolean {
    const publicUrls = ['/assets', '/public', '/login', '/register'];
    return publicUrls.some((publicUrl) => url.includes(publicUrl));
  }
}
