import {Component, OnInit} from '@angular/core';
import {ConfirmationService, MenuItem, MessageService} from "primeng/api";
import {CohortService} from "../../../services/cohort/cohort.service";
import {Cohort} from "../../../../model/cohort";
import {Router} from "@angular/router";

@Component({
  selector: 'app-cohort-list',
  templateUrl: './cohort-list.component.html',
  styleUrl: './cohort-list.component.css'
})
export class CohortListComponent implements OnInit{
  items: MenuItem[] | undefined;
  loading: boolean = true;
  cohorts!: Cohort[]

  selectedCohorts!: any;
  searchQuery!:string;

  first = 0;
  rows = 10;


  constructor(private messageService: MessageService,
              private cohortService: CohortService,
              private router: Router,
              private confirmationService: ConfirmationService) {
  }

  ngOnInit() {

    this.items =
      [
        { icon: 'pi pi-home', route: '/' },
        { label: 'Quản trị nhóm'}
      ];

    this.cohortService.getAllCohorts().subscribe((result) => {
      if(result.status == 1){
        this.cohorts = result.data;
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

  pageChange(event: any) {
    this.first = event.first;
    this.rows = event.rows;
  }

  clickAddCohort(){
    this.router.navigate(['admin/cohort-add'])
  }

  clickCohortDetail(cohortId: any){
    this.router.navigate(['admin/cohort-detail', cohortId]);
  }

  clickDeleteCohorts() {
    let cohortIds: any = []

    for (const cohort in this.selectedCohorts) {
      cohortIds.push(this.selectedCohorts[cohort].id)
    }

    if(cohortIds.length == 0) {
      return this.messageService.add({
        severity: 'warn',
        summary: 'Chưa chọn',
        detail: 'Vui lòng chọn nhóm để xóa',
        life: 2000
      });
    }

    this.confirmationService.confirm({
      message: `Bạn có chắc chắn muốn xóa ${cohortIds.length} nhóm này?`,
      header: 'Xác nhận xóa',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.cohortService.deleteCohorts(cohortIds).subscribe((result) => {
          if(result.status == 1) {
            this.messageService.add({
              severity: 'success',
              summary: 'Thành công',
              detail: 'Xóa thành công',
              life: 2000
            });
            this.resetData();
          }
        }, (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Không thể xóa nhóm',
            life: 2000
          });
        });
      }
    });
  }

  resetData(){
    this.cohorts = [];
    this.selectedCohorts = null;
    this.ngOnInit()
  }


}
