import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExamResultManagementComponent } from './exam-result-management.component';

describe('ExamResultManagementComponent', () => {
  let component: ExamResultManagementComponent;
  let fixture: ComponentFixture<ExamResultManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ExamResultManagementComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ExamResultManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
