import {Component, OnInit} from '@angular/core';
import {MenuItem} from "primeng/api";
import {RoleService} from "../../../services/role/role.service";

@Component({
  selector: 'app-resource-list',
  templateUrl: './resource-list.component.html',
  styleUrl: './resource-list.component.css'
})
export class ResourceListComponent implements OnInit{
  items: MenuItem[] | undefined;
  active = 1

  resources!: any[]
  loading = true;

  first = 0;
  rows = 10;
  searchQuery!:string;

  constructor(private roleService: RoleService) {
  }
  ngOnInit() {
    this.items =
      [
        { icon: 'pi pi-home', route: '/' },
        { label: 'Quản lý tài nguyên', url: '/admin/resource-management' }
      ];

    this.roleService.getClientResources().subscribe((rs) => {
      this.resources = rs.data;
      this.loading = false;

    })
  }


  pageChange(event: any) {
    this.first = event.first;
    this.rows = event.rows;
  }

  convertScopes(scopes: any[]){
    let rs = "";
    for (let i = 0; i < scopes.length; i++) {
      rs += scopes[i].name + " ";
    }
    return rs;
  }



}
