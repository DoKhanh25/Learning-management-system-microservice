// multiple-video-player.component.ts
import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnDestroy,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import {VgApiService} from '@videogular/ngx-videogular/core';
import { LessonPages } from '../../../model/course';
import {CourseService} from "../../services/course/course.service";
import {Subject, takeUntil } from "rxjs";

@Component({
  selector: 'app-multiple-video-player',
  templateUrl: './multiple-video-player.component.html',
  styleUrls: ['./multiple-video-player.component.css']
})
export class MultipleVideoPlayerComponent implements OnChanges , OnDestroy{
  @Input() lessonPages!: LessonPages;


  api!: VgApiService;
  safeVideoUrl: SafeResourceUrl | null = null;
  isEmbeddedVideo: boolean = false;
  videoType: string = 'video/mp4';
  loading: boolean = false;
  errorMessage: string = '';
  videoBlobUrl: string | null = null;
  totalBytes: number = 0;
  loadedBytes: number = 0;
  private destroy$ = new Subject<void>();


  @ViewChild('myMedia') myMedia!: ElementRef<HTMLVideoElement>;


  constructor(
    private sanitizer: DomSanitizer,
    private courseService: CourseService,
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['lessonPages'] && this.lessonPages) {
      this.loading = true;
      this.processVideoContent();
    }
  }


  onPlayerReady(api: VgApiService): void {
    this.api = api;
    if (this.api && this.api.getDefaultMedia()) {
      const media = this.api.getDefaultMedia();
      if (media) {
        media.subscriptions.loadedMetadata.pipe(
          takeUntil(this.destroy$)
        ).subscribe(
          () => {
            this.playVideo();
            this.loading = false;
          },
          (error) => {
            this.errorMessage = 'Error loading metadata: ' + error;
            this.loading = false;
          }
        );
        media.subscriptions.loadedData.pipe(
          takeUntil(this.destroy$)
        ).subscribe(
          () => {
            this.loading = false;
          },
          (error) => {
            this.errorMessage = 'Error loading data: ' + error;
            this.loading = false;
          }
        );
      } else {
        this.errorMessage = 'Media not available';
        this.loading = false;
      }
    } else {
      this.errorMessage = 'Video API not initialized';
      this.loading = false;
    }
  }



  playVideo(): void {
    if (this.api && this.api.getDefaultMedia()) {
      this.api.play();
    }
  }

  private processVideoContent(): void {
    if (this.lessonPages.content?.includes('http') || this.lessonPages.content?.includes('https')) {
      this.handleEmbeddedVideo();
    } else {
      this.handleStreamedVideo();
    }
  }

  private handleEmbeddedVideo(): void {
    let videoUrl = this.lessonPages.content || "";

    if (videoUrl.includes('youtube.com') || videoUrl.includes('youtu.be')) {
      videoUrl = this.convertYouTubeToEmbedUrl(videoUrl);
    }

    this.safeVideoUrl = this.sanitizer.bypassSecurityTrustResourceUrl(videoUrl);
    this.isEmbeddedVideo = true;
    this.loading = false;
  }

  private convertYouTubeToEmbedUrl(url: string): string {
    const youtubePatterns = [
      /(?:https?:\/\/)?(?:www\.)?youtube\.com\/watch\?v=([^&]+)/,
      /(?:https?:\/\/)?youtu\.be\/([^?]+)/,
      /(?:https?:\/\/)?(?:www\.)?youtube\.com\/embed\/([^?]+)/
    ];

    for (const pattern of youtubePatterns) {
      const match = url.match(pattern);
      if (match && match[1]) {
        return `https://www.youtube.com/embed/${match[1]}`;
      }
    }

    return url;
  }


  private handleStreamedVideo(): void {
    this.streamVideo(0, this.lessonPages.id);
  }

  private streamVideo(startByte: any, lessonPageId: any): void {
    const range = `bytes=${startByte}-`;

    this.courseService.getVideoFileLessonPagesById(lessonPageId, range)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          const contentRange = response.headers.get('Content-Range');
          const contentLength = response.headers.get('Content-Length');

          if (contentRange) {
            const [, total] = contentRange.match(/bytes \d+-\d+\/(\d+)/) || [];
            this.totalBytes = parseInt(total, 10);
          }

          this.loadedBytes = parseInt(contentLength || '0', 10);

          const blob = new Blob([response.body!], { type: this.videoType });
          this.videoBlobUrl = URL.createObjectURL(blob);
          this.safeVideoUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.videoBlobUrl);
          this.isEmbeddedVideo = false;
          this.loading = false;
        },
        error: (error) => {
          this.errorMessage = 'Error loading video: ' + error.message;
          this.loading = false;
        }
      });
  }

  onTimeUpdate(event: any): void {
    if (this.api && this.api.getDefaultMedia()) {
      const video: HTMLVideoElement = this.myMedia.nativeElement;
      const bufferThreshold = 30;

      if (video.duration - video.currentTime < bufferThreshold &&
        this.loadedBytes < this.totalBytes) {
        this.streamVideo(this.loadedBytes, this.lessonPages.id);
      }
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.videoBlobUrl) {
      URL.revokeObjectURL(this.videoBlobUrl);
    }
  }

  formatBytes(bytes: number, decimals: number = 1): string {
    if (bytes === 0) return '0 Bytes';

    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(decimals)) + ' ' + sizes[i];
  }

}
