import {Component, OnDestroy, OnInit} from '@angular/core';
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/api";
import { DomSanitizer, SafeResourceUrl } from "@angular/platform-browser";
import { CourseService } from "../../../services/course/course.service";
import { ConfirmationService } from 'primeng/api';
import {LessonBranch, LessonNote, LessonTimer} from "../../../../model/course";
import {AuthService} from "../../../services/auth/auth.service";
import {KeycloakService} from "keycloak-angular";



@Component({
  selector: 'app-lesson-detail',
  templateUrl: './lesson-detail.component.html',
  styleUrl: './lesson-detail.component.css'
})
export class LessonDetailComponent implements OnInit, OnDestroy {
  lessonId: number | null = null;
  lesson: any = null;
  lessonPages: any[] = [];
  currentPageIndex: number = 0;
  loading: boolean = true;
  progress: number = 0;
  completedPages: Set<number> = new Set();

  showChat: boolean = false;

  // For video/document tracking
  videoUrl: string | null = null;
  documentPath: string | null = null;
  documentFileName: string = '';

  // Notes functionality
  currentPageNotes: string = '';
  userNotes: LessonNote[] = [];
  hasNoteChanges: boolean = false;
  lessonNoteId: number | null = null;

  // tracking student viewing page
  private pageStartTime: number = 0;
  private currentPageId: number | null = null;


  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private courseService: CourseService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private sanitizer: DomSanitizer,
    private authService: AuthService,
    private keycloakService: KeycloakService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.lessonId = +params['id'];
        this.loadLesson();
        this.loadUserNotes();
        this.postLessonTimer();
      }
    });

    window.addEventListener('beforeunload', this.saveBeforeUnload);

  }

  postLessonTimer(){
    if(!this.keycloakService.getUserRoles().includes('ROLE_TEACHER')
    && this.keycloakService.getUserRoles().includes('ROLE_ADMIN'))
    {
      let lessonTimer: LessonTimer = {
        lessonId: this.lessonId!,
        userId: this.authService.getUserIdFromCookie()!,
        startTime: Date.now()
      }
      this.courseService.saveLessonTimer(lessonTimer).subscribe(
        (response) => {
        },
        (error) => {
          console.log(error)
        }
      )
    }
  }

  loadLesson(): void {
    if (!this.lessonId) return;

    this.loading = true;
    this.courseService.getLessonById(this.lessonId).subscribe({
      next: (data) => {
        this.lesson = data;
        this.loadLessonPages();
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load lesson'
        });
        this.loading = false;
      }
    });
  }

  loadLessonPages(): void {
    if (!this.lessonId) return;

    this.courseService.getLessonPagesByLessonId(this.lessonId).subscribe({
      next: (pages) => {
        this.lessonPages = pages.data.sort((a: any, b: any) => a.position - b.position);
        this.loading = false;
        if (this.lessonPages.length > 0) {
          this.showPage(0);
        }
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load lesson pages'
        });
        this.loading = false;
      }
    });
  }

  showPage(index: number): void {
    if (index >= 0 && index < this.lessonPages.length) {

      this.saveTimeSpentOnPage();


      // Save current notes if there are changes
      if (this.hasNoteChanges) {
        this.saveNotes();
      }

      this.currentPageIndex = index;
      const page = this.getCurrentPage();

      // Start timing for the new page
      this.currentPageId = page.id;
      this.pageStartTime = Date.now();


      // Reset content holders
      this.videoUrl = null;
      this.documentPath = null;
      this.currentPageNotes = '';
      this.hasNoteChanges = false;

      // Set content based on page type
      switch (page.qType) {
        case 'VIDEO':
          this.videoUrl = page.content;
          break;
        case 'DOCUMENT':
          this.documentPath = page.content;
          this.documentFileName = page.title;
          break;
      }

      // Load notes for current page
      this.loadPageNotes(page.id);

      // Mark page as completed
      this.markPageAsCompleted(page.id);
    }
  }

  navigateToPage(index: number): void {
    this.showPage(index);
  }

  getCurrentPage(): any {
    return this.lessonPages[this.currentPageIndex] || null;
  }

  markPageAsCompleted(pageId: number): void {
    this.completedPages.add(pageId);
    this.updateProgress();
  }

  updateProgress(): void {
    if (this.lessonPages.length === 0) return;
    this.progress = (this.completedPages.size / this.lessonPages.length) * 100;
    if(this.progress == 100){
      let lessonTimer: LessonTimer = {
        lessonId: this.lessonId!,
        completed: 1
      }
      this.courseService.saveLessonTimer(lessonTimer).subscribe((response) => {
      }, (error) => {
        console.log(error)
      })
    }
  }

  isPageCompleted(pageId: number): boolean {
    return this.completedPages.has(pageId);
  }


  getTagSeverity(type: string): any {
    switch (type) {
      case 'CONTENT': return 'info';
      case 'VIDEO': return 'success';
      case 'DOCUMENT': return 'warning';
      default: return 'info';
    }
  }

  getLessonProgress(): number {
    return Math.round(this.progress);
  }

  // Notes functionality
  loadUserNotes(): void {
    // Load from localStorage or from API
    const savedNotes = localStorage.getItem(`lesson_notes_${this.lessonId}`);
    if (savedNotes) {
      this.userNotes = JSON.parse(savedNotes);
    }
  }

  loadPageNotes(lessonPageId: number): void {
    this.courseService.getLessonNoteByLessonPageIdAndUserId(lessonPageId).subscribe(
      (response) => {
        if(response.status === 1) {
          this.currentPageNotes = response.data.note;
          this.lessonNoteId = response.data.id;
          this.hasNoteChanges = false;
        }
      },
      (error) => {
        console.log(error);
      }
    )
  }

  onNotesChanged(e: any): void {
    this.hasNoteChanges = true;
  }

  toggleChat(): void {
    this.showChat = !this.showChat;
  }

  saveNotes(): void {
    const currentPage = this.getCurrentPage();
    if (!currentPage) return;
    const pageId = currentPage.id;


    let lessonNote: LessonNote = {
      lessonPagesId: pageId,
      note: this.currentPageNotes,
    }
    this.courseService.saveLessonNote(lessonNote).subscribe(
      (response) => {
        if(response.status === 1) {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Notes saved successfully'
          });
          this.hasNoteChanges = false;
        }
      },
      (error) => {
        console.log(error);
      }
    )


  }

  clearNotes(): void {
    this.confirmationService.confirm({
      message: 'Are you sure you want to clear your notes?',
      header: 'Clear Notes',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.courseService.deleteLessonNote(this.lessonNoteId).subscribe(
          (response) => {
            if(response.status === 1) {
              this.messageService.add({
                severity: 'success',
                summary: 'Success',
                detail: 'Notes cleared successfully'
              });
              this.hasNoteChanges = true;
              this.lessonNoteId = null;
              this.currentPageNotes = '';
            }
          }
        )
      }
    });
  }

  private saveTimeSpentOnPage(): void {
    if (this.currentPageId && this.pageStartTime > 0) {
      const timeSpent = Date.now() - this.pageStartTime;

      // Only save if they spent at least 1 second
      if (timeSpent >= 1000) {
        const lessonBranchData: LessonBranch = {
          lessonId: this.lessonId!,
          lessonPagesId: this.currentPageId,
          timeSeen: Math.floor(timeSpent / 1000) // Convert to seconds
        };

        this.courseService.saveLessonBranch(lessonBranchData).subscribe({
          next: (response) => {
            console.log('Time spent saved successfully');
          },
          error: (err) => {
            console.error('Failed to save time spent', err);
          }
        });
      }
    }
  }
  private saveBeforeUnload = (): void => {
    this.saveTimeSpentOnPage();
  };

  ngOnDestroy(): void {
    this.saveTimeSpentOnPage();
    window.removeEventListener('beforeunload', this.saveBeforeUnload);
  }
}
