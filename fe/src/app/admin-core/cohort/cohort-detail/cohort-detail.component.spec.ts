import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CohortDetailComponent } from './cohort-detail.component';

describe('CohortDetailComponent', () => {
  let component: CohortDetailComponent;
  let fixture: ComponentFixture<CohortDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CohortDetailComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CohortDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
