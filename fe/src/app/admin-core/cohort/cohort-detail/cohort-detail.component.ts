import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {MenuItem, MessageService} from "primeng/api";
import {User} from "../../../../model/user";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {UserService} from "../../../services/user-service/user.service";
import {CohortService} from "../../../services/cohort/cohort.service";
import {ActivatedRoute} from "@angular/router";
import {forkJoin} from "rxjs";

@Component({
  selector: 'app-cohort-detail',
  templateUrl: './cohort-detail.component.html',
  styleUrl: './cohort-detail.component.css'
})
export class CohortDetailComponent implements OnInit {
  items: MenuItem[] | undefined;
  sourceUsers!: User[];
  targetUsers!: User[];
  cohortUpdateForm!: FormGroup;
  cohortId!: any;

  constructor(private fb: FormBuilder,
              private route: ActivatedRoute,
              private userService: UserService,
              private messageService: MessageService,
              private cdr: ChangeDetectorRef,
              private cohortService: CohortService) {
      this.cohortId = this.route.snapshot.paramMap.get('id');

  }

    ngOnInit() {
        this.items =
            [
                { icon: 'pi pi-home', route: '/' },
                { label: 'Quản trị nhóm', url: '/admin/cohort-management'},
                {label: 'Thêm nhóm'}
            ];

        this.targetUsers = [];

        this.cohortUpdateForm = this.fb.group({
            name: [{value: "", disabled: true}, Validators.required],
            description: [""]
        })

      forkJoin({
        cohort: this.cohortService.getCohortById(this.cohortId),
        users: this.userService.getAllUsers()
      }).subscribe({
        next: ({ cohort, users }) => {
          if (cohort.status === 1) {
            this.cohortUpdateForm.setValue({
              name: cohort.data.name,
              description: cohort.data.description
            });
          }

          if (users.status === 1) {
            this.sourceUsers = users.data;
          }
          console.log(cohort.data)

          this.processPickListData(cohort.data.cohortMembers, users.data);
        },
      })
    }

  processPickListData(cohortMembers: any, users: any){
    if(cohortMembers.length == 0) return;
    for (let i = 0; i < users.length; i++) {
      for (let j = 0; j < cohortMembers.length; j++) {
        if(users[i].userId == cohortMembers[j].keycloakId){
          this.targetUsers.push(users[i]);
          this.sourceUsers = this.sourceUsers.filter(item => item != users[i]);
        }
      }
    }
  }

  clickUpdateCohort(){
    let cohort = {
      id: this.cohortId,
      available: 1,
      description: this.cohortUpdateForm.get("description")?.value,
      userIds: this.getUserIds(this.targetUsers)
    }

    this.cohortService.updateCohort(cohort).subscribe((result) => {
      if(result.status == 1){
        this.messageService.add({
          severity: 'success',
          summary: 'Thành công',
          detail: 'Tạo thành công',
          life: 2000
        })
      }
      this.resetData()
    }, (error) => {
      this.messageService.add({
        severity: 'error',
        summary: 'Lỗi',
        detail: 'Không thành công',
        life: 2000
      })
    })
  }


  getUserIds(users: User[]){
    if(users.length == 0){
      return null;
    }
    let userIds = [];
    for (let i = 0; i < users.length; i++) {
      userIds.push(users[i].userId);
    }
    return userIds;
  }

  resetData(){
    this.sourceUsers = [];
    this.targetUsers = [];
    this.ngOnInit()
  }


}
