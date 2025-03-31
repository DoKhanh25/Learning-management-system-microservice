import { Pipe, PipeTransform } from '@angular/core';
import {DomSanitizer, SafeHtml} from "@angular/platform-browser";

@Pipe({
  name: 'boldMarkdown',
  standalone: true
})
export class BoldMarkdownPipe implements PipeTransform {
  constructor(private sanitizer: DomSanitizer) {}

  transform(value: string): SafeHtml {
    if (!value) return this.sanitizer.bypassSecurityTrustHtml('');

    // Tách các dòng
    const lines = value.split('\n');
    let htmlOutput = '';
    let inList = false;

    for (const line of lines) {
      // Xử lý dòng bắt đầu bằng '*'
      if (line.trim().startsWith('*')) {
        if (!inList) {
          htmlOutput += '<ul>';
          inList = true;
        }
        // Loại bỏ '* ' ở đầu dòng và xử lý in đậm
        const content = line.replace(/^\*\s*/, '');
        const boldContent = content.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
        htmlOutput += `<li>${boldContent}</li>`;
      } else {
        // Kết thúc danh sách nếu đang trong danh sách
        if (inList) {
          htmlOutput += '</ul>';
          inList = false;
        }
        // Xử lý in đậm cho dòng thường
        const boldContent = line.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
        htmlOutput += `<p>${boldContent}</p>`;
      }
    }

    // Đóng danh sách nếu vẫn đang mở
    if (inList) {
      htmlOutput += '</ul>';
    }

    return this.sanitizer.bypassSecurityTrustHtml(htmlOutput);
  }
}
