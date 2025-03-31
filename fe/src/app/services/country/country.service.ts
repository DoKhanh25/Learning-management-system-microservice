import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class CountryService {
    private apiUrl = 'https://restcountries.com/v3.1/all';
    private cityApiUrl = 'https://countriesnow.space/api/v0.1/countries/cities';
    getCities(country: string): Observable<any> {
        return this.http.post<any>(this.cityApiUrl, { country });
    }
    constructor(private http: HttpClient) {}

    getCountries(): Observable<any[]> {
        return this.http.get<any[]>(this.apiUrl);
    }
}
