import {Component, OnInit} from '@angular/core';
import {MenuItem, MessageService} from "primeng/api";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Policy, Role} from "../../../../model/role";
import {RoleService} from "../../../services/role/role.service";

@Component({
  selector: 'app-policy-add',
  templateUrl: './policy-add.component.html',
  styleUrl: './policy-add.component.css'
})
export class PolicyAddComponent implements OnInit{
  items: MenuItem[] | undefined;
  policyForm!: FormGroup;
  selectedLogic:string = "POSITIVE";

  allRoles!: Role[];
  selectedRoles: Role[] = []
  selectedAssignRole: Role[] = [];

  first = 0;
  rows = 10;

  searchQuery!:string;
  visible = false;

  policyPost!: Policy;


  constructor(private fb: FormBuilder,
              private roleService: RoleService,
              private messageService: MessageService) {
  }
  ngOnInit() {
    this.items =
      [
        { icon: 'pi pi-home', route: '/' },
        { label: 'Quản lý chính sách', url: '/admin/policy-management' },
        { label: 'Tạo chính sách' }
      ];

    this.policyForm = this.fb.group({
      name: ["", [Validators.required]],
      description: [""]
    })

    this.roleService.getAllRoles().subscribe((result) => {
      this.allRoles = result.data
    })
  }

  clickPostPolicy(){
    let roleIds: any[] = [];
    for(let i=0; i< this.selectedRoles.length; i++){
      roleIds.push({
        id: this.selectedRoles[i].id,
        required: false
      })
    }

    this.policyPost = {
      name: this.policyForm.get('name')?.value,
      logic: this.selectedLogic,
      description: this.policyForm.get('description')?.value,
      config: {
        roles: JSON.stringify(roleIds),
        fetchRoles: false
      },
      type: "role"
    }

    this.roleService.addClientPolicy(this.policyPost).subscribe((result) => {
      if(result.status == 1){
        this.messageService.add(
          {severity: 'success',
            summary: 'Thành công',
            detail: 'Cập nhật thành công',
            life: 2000
          });
      } else {
        this.messageService.add(
          {severity: 'warn',
            summary: 'Không thành công',
            detail: result.data,
            life: 2000
          });
      }
    }, (err) => {
      this.messageService.add(
        {severity: 'warn',
          summary: 'Không thành công',
          detail: err.detail,
          life: 2000
        });
    })
  }
  clickRemoveRole(name: string){
    this.selectedRoles = this.selectedRoles.filter((role)=> {
      return role.name != name;
    });
  }

  pageChange(event: any) {
    this.first = event.first;
    this.rows = event.rows;
  }

  clickAssignRole(){
    this.selectedAssignRole.forEach((e) => {
      if(!this.isContainName(e.name)){
        this.selectedRoles.push(e);
      }
    })
    this.visible = false;
    this.selectedAssignRole = [];
  }

  isContainName(name: string | undefined){
    for(let i = 0; i < this.selectedRoles.length; i ++ ){
      if(this.selectedRoles[i].name == name){
        return true;
      }
    }
    return false;
  }


}
