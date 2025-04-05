import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExamMultipleChoiceComponent } from './exam-multiple-choice.component';

describe('ExamMultipleChoiceComponent', () => {
  let component: ExamMultipleChoiceComponent;
  let fixture: ComponentFixture<ExamMultipleChoiceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ExamMultipleChoiceComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ExamMultipleChoiceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
