import {Component, Input, OnChanges, OnDestroy, SimpleChanges} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DomSanitizer, SafeHtml, SafeResourceUrl } from '@angular/platform-browser';
import mammoth from 'mammoth';
import { LessonPages } from '../../../model/course';
import {CourseService} from "../../services/course/course.service";

@Component({
  selector: 'app-document-viewer',
  templateUrl: './document-viewer.component.html',
  styleUrls: ['./document-viewer.component.css']
})
export class DocumentViewerComponent implements OnChanges {
  @Input() lessonPages!: LessonPages;

  documentUrl: any = null;
  docContent: SafeHtml | null = null;
  pptViewerUrl: SafeResourceUrl | null = null;
  loading: boolean = false;
  isPdf: boolean = false;
  isDocx: boolean = false;



// Add this to your imports

  page: number = 1;
  totalPages: number = 0;
  zoom: number = 1;
  isFullscreen: boolean = false;
  originalPdfBlob: Blob | null = null;

  constructor(
    private http: HttpClient,
    private sanitizer: DomSanitizer,
    private courseService: CourseService
  ) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['lessonPages'] && this.lessonPages) {
      this.loadDocument();
    }
  }

  loadDocument() {
    if (!this.lessonPages || !this.lessonPages.id) return;

    this.loading = true;
    this.isPdf = this.lessonPages.content?.endsWith('.pdf') || false;
    this.isDocx = this.lessonPages.content?.endsWith('.docx') || this.lessonPages.content?.endsWith('.doc') || false;

    if (!(this.isPdf || this.isDocx)) {
      this.loading = false;
      return;
    }

    const apiUrl = `http://localhost:8090/course/api/getDocumentFileByLessonPagesId?id=${this.lessonPages.id}`;

    if (this.isPdf) {
      // Tải PDF dưới dạng Blob để dùng với ng2-pdf-viewer
      this.http.get(apiUrl, { responseType: 'blob' }).subscribe({
        next: (blob: Blob) => {
          const url = URL.createObjectURL(blob);
          this.originalPdfBlob = blob;
          this.documentUrl = url;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error fetching PDF:', error);
          this.loading = false;
        }
      });
    } else if (this.isDocx) {
      // Tải DOCX dưới dạng ArrayBuffer để xử lý với mammoth
      this.http.get(apiUrl, { responseType: 'arraybuffer' }).subscribe({
        next: (data: ArrayBuffer) => {
          mammoth.convertToHtml({ arrayBuffer: data }, {
            styleMap: [
              "p[style-name='Title'] => h1.title:fresh",
              "p[style-name='Heading 1'] => h1.title:fresh",
              "p[style-name='Heading 2'] => h2.subtitle:fresh",
              "p[style-name='Heading 3'] => h3.section:fresh",
              "p[style-name='Heading 4'] => h4:fresh",
              "p => p.text:fresh",
              "p[style-name='List Paragraph'] => li.list-item:fresh",
              "p[style-name='List 1'] => li.list-item:fresh",
              "p[style-name='List 2'] => li.list-item:fresh",
              "p[style-name='Numbered List'] => li.numbered-item:fresh",
              "b => strong.bold",
              "i => em.italic",
              "u => span.underline",
              "strike => span.strike",
              "table => table.doc-table:fresh",
              "tr => tr.table-row:fresh",
              "td => td.table-cell:fresh",
              "th => th.table-header:fresh",
              "p[style-name='Quote'] => blockquote.quote:fresh",
              "p[style-name='Intense Quote'] => blockquote.quote:fresh",
              "r[style-name='Hyperlink'] => a"
            ],
            ignoreEmptyParagraphs: true,
            includeDefaultStyleMap: true,
            convertImage: mammoth.images.imgElement((image) => {
              return image.read("base64").then((base64String) => {
                return {
                  src: `data:${image.contentType};base64,${base64String}`,
                  style: "max-width: 100%; max-height: 300px; width: auto; height: auto;" // Thêm style trực tiếp vào thẻ img
                };
              });
            })
          })
            .then(result => {
              // Wrap the result in a container div for better styling control
              const enhancedHtml = `<div class="docx-document">${result.value}</div>`;
              this.docContent = this.sanitizer.bypassSecurityTrustHtml(enhancedHtml);
              this.loading = false;
            })
        },
        error: (error) => {
          console.error('Error fetching DOCX:', error);
          this.loading = false;
        }
      });
    }
  }


  afterLoadComplete(pdfData: any) {
    this.totalPages = pdfData.numPages;
  }

  nextPage() {
    if (this.page < this.totalPages) {
      this.page++;
    }
  }

  prevPage() {
    if (this.page > 1) {
      this.page--;
    }
  }

  zoomIn() {
    if (this.zoom < 3) {
      this.zoom += 0.1;
    }
  }

  zoomOut() {
    if (this.zoom > 0.5) {
      this.zoom -= 0.1;
    }
  }

  pageRendered() {
    // Could add page rendering optimization here
  }

  onError(error: any) {
    console.error('PDF error:', error);
  }

  toggleFullscreen() {
    const pdfContainer = document.querySelector('.pdf-container') as HTMLElement;

    if (!this.isFullscreen) {
      if (pdfContainer.requestFullscreen) {
        pdfContainer.requestFullscreen();
      }
    } else {
      if (document.exitFullscreen) {
        document.exitFullscreen();
      }
    }

    this.isFullscreen = !this.isFullscreen;
  }

  downloadPdf() {
    if (this.originalPdfBlob) {
      const url = window.URL.createObjectURL(this.originalPdfBlob);
      const link = document.createElement('a');
      link.href = url;
      link.download = this.lessonPages.content || 'document.pdf';
      link.click();
      window.URL.revokeObjectURL(url);
    } else if (typeof this.documentUrl === 'string') {
      const link = document.createElement('a');
      link.href = this.documentUrl;
      link.download = this.lessonPages.content || 'document.pdf';
      link.click();
    }
  }








}
