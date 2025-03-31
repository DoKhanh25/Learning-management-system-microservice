import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LessonStudentManagementComponent } from './lesson-student-management.component';

describe('LessonStudentManagementComponent', () => {
  let component: LessonStudentManagementComponent;
  let fixture: ComponentFixture<LessonStudentManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LessonStudentManagementComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(LessonStudentManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
