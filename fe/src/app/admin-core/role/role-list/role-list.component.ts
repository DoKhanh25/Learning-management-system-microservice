import {Component, OnInit} from '@angular/core';
import {MenuItem, MessageService} from "primeng/api";
import {User} from "../../../../model/user";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {RoleService} from "../../../services/role/role.service";
import {Role, RolePost} from "../../../../model/role";
import {Router} from "@angular/router";

@Component({
  selector: 'app-role-list',
  templateUrl: './role-list.component.html',
  styleUrl: './role-list.component.css'
})
export class RoleListComponent implements OnInit{
  items: MenuItem[] | undefined;

  selectedRoles!: any;
  roles!: Role[]

  loading: boolean = true;

  first = 0;
  rows = 10;


  searchQuery!:string;

  visible = false;

  roleAddForm!: FormGroup


  constructor(private roleService: RoleService,
              private router: Router,
              private fb: FormBuilder,
              private messageService: MessageService) {

  }
  ngOnInit() {
    this.items =
      [
        { icon: 'pi pi-home', route: '/' },
        { label: 'Quản lý vai trò', url: '/admin/role-management' }
      ];

    this.roleAddForm = this.fb.group({
      name: ['', Validators.required],
      description: ['']
    })


    this.roleService.getAllRoles().subscribe((result) => {
      console.log(result.data)
      this.roles = result.data.filter((e: any) => e.name.includes("ROLE_"));
      this.loading = false;
    }, (err) => {
      console.log(err)
    })
  }
  clickOpenRoleForm(){
    this.visible = true;
  }
  clickAddRole(){
    let role: RolePost = {
      name: this.roleAddForm.get('name')?.value,
      description: this.roleAddForm.get('description')?.value
    }
    this.roleService.addRole(role).subscribe((result) => {
      if(result.status == 1){
        this.messageService.add(
          {severity: 'success',
            summary: 'Thành công',
            detail: 'Thành công',
            life: 2000
          });
        this.reset();
      }
    }, (err) => {
      this.messageService.add(
        {severity: 'danger',
          summary: 'danger',
          detail: err.detail,
          life: 2000
        });
    })
  }
  clickDeleteRole(){
    let roleNames = []

    for (const role in this.selectedRoles) {
      roleNames.push(this.selectedRoles[role].name);
    }
    this.roleService.deleteRoles(roleNames).subscribe((rs) => {
      if(rs.status == 1){
        this.messageService.add(
          {severity: 'success',
            summary: 'Thành công',
            detail: 'Thành công',
            life: 2000
          });
        this.reset();
      }
    }, (error) => {
      console.log(error)
      this.messageService.add(
        {severity: 'danger',
          summary: 'danger',
          detail: error.detail,
          life: 2000
        });
      this.reset();
    })
  }

  reset(){
    this.visible = false;
    this.ngOnInit()
  }




  pageChange(event: any) {
    this.first = event.first;
    this.rows = event.rows;
  }

  clickRoleDetail(roleName: any){
    this.router.navigate(['admin/role-detail', roleName]);
  }

}
