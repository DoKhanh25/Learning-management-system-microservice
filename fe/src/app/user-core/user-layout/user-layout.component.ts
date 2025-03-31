import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-user-layout',
  templateUrl: './user-layout.component.html',
  styleUrl: './user-layout.component.css'
})
export class UserLayoutComponent implements OnInit{
  ngOnInit(): void {
    // Listen for chat toggle events to adjust main content
    document.addEventListener('DOMContentLoaded', () => {
      const chatContainer = document.querySelector('.chat-container');
      const appMainContent = document.querySelector('.app-main-content');

      if (chatContainer && appMainContent) {
        const observer = new MutationObserver((mutations) => {
          mutations.forEach((mutation) => {
            if (mutation.attributeName === 'class') {
              const hasClosed = chatContainer.classList.contains('chat-closed');
              if (hasClosed) {
                appMainContent.classList.remove('chat-open');
              } else {
                appMainContent.classList.add('chat-open');
              }
            }
          });
        });

        observer.observe(chatContainer, { attributes: true });
      }
    });
  }

}
