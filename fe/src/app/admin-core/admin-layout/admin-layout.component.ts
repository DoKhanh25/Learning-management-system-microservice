import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-admin-layout',
  templateUrl: './admin-layout.component.html',
  styleUrl: './admin-layout.component.css'
})
export class AdminLayoutComponent implements OnInit{
  ngOnInit(): void {
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
