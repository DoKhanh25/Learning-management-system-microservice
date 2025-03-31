import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {map, Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class DiscoveryService {
  private readonly eurekaUrl = 'http://localhost:8090/eureka/apps';

  constructor(private http: HttpClient) {}

  // Lấy danh sách tất cả dịch vụ từ Eureka (trả về JSON)
  getAllServices(): Observable<any> {
    const headers = new HttpHeaders({
      'Accept': 'application/json' // Yêu cầu trả về JSON thay vì XML
    });

    return this.http.get(this.eurekaUrl, { headers }).pipe(
      map((response: any) => {
        return response.applications.application;
      })
    );
  }

  getServiceByName(appName: string): Observable<any> {
    const headers = new HttpHeaders({
      'Accept': 'application/json'
    });

    return this.http.get(`${this.eurekaUrl}/${appName.toUpperCase()}`, { headers }).pipe(
      map((response: any) => {
        return response.application;
      })
    );
  }


}
