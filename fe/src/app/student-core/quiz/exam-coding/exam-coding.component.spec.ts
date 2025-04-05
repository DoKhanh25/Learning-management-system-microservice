import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExamCodingComponent } from './exam-coding.component';

describe('ExamCodingComponent', () => {
  let component: ExamCodingComponent;
  let fixture: ComponentFixture<ExamCodingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ExamCodingComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ExamCodingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
