import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CourseStatisticComponent } from './course-statistic.component';

describe('CourseStatisticComponent', () => {
  let component: CourseStatisticComponent;
  let fixture: ComponentFixture<CourseStatisticComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CourseStatisticComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CourseStatisticComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
