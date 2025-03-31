import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AiChatAssistantComponent } from './ai-chat-assistant.component';

describe('AiChatAssistantComponent', () => {
  let component: AiChatAssistantComponent;
  let fixture: ComponentFixture<AiChatAssistantComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AiChatAssistantComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AiChatAssistantComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
