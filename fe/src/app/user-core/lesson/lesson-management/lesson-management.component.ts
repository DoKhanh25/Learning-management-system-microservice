import {ChangeDetectorRef, Component, OnInit, Pipe, PipeTransform} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {CourseService} from "../../../services/course/course.service";
import {ConfirmationService, MessageService} from "primeng/api";
import {CdkDragDrop, moveItemInArray} from "@angular/cdk/drag-drop";
import {DomSanitizer} from "@angular/platform-browser";
import {LessonPages} from "../../../../model/course";
import {debounceTime, distinctUntilChanged} from "rxjs";



@Component({
  selector: 'app-lesson-management',
  templateUrl: './lesson-management.component.html',
  styleUrl: './lesson-management.component.css'
})
export class LessonManagementComponent implements OnInit{
  lessonId: string | null = null;
  lesson: any = null;
  lessonPages: LessonPages[] = [];
  loading = false;
  displayPageDialog = false;
  pageForm: FormGroup;
  editingPageId: number | null = null;
  contentTypeOptions = [
    { label: 'Văn bản', value: 'CONTENT' },
    { label: 'Video', value: 'VIDEO' },
    {label: 'Tài liệu', value: 'DOCUMENT'}
    // { label: 'Programming Exercise', value: 'PROGRAMMING' },
    // { label: 'Short Answer', value: 'SHORT_ANS' }
  ];

  videoSourceOptions = [
    { label: 'YouTube/Vimeo URL', value: 'URL' },
    { label: 'Upload Video', value: 'UPLOAD' }
  ];

  selectedVideoSource: string = 'URL';
  uploadedVideoFile: File | null = null;
  uploadedVideoName: string = '';
  uploadedVideoUrl: string = '';

  videoEmbedUrl: string | null = null; // URL gốc dạng chuỗi


  uploadedFile: File | null = null;
  uploadedFileName: string = '';
  uploadedFileUrl: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private courseService: CourseService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private fb: FormBuilder,
    private sanitizer: DomSanitizer,
    private cdr: ChangeDetectorRef
  ) {
    this.pageForm = this.fb.group({
      title: ['', Validators.required],
      qType: ['CONTENT', Validators.required],
      content: [''],
      position: [0]
    });
  }

  ngOnInit() {
    this.lessonId = this.route.snapshot.paramMap.get('id');
    this.pageForm.get('content')?.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe((value) => this.updateVideoEmbedUrl(value));
    this.loadLessonData();
  }

  updateVideoEmbedUrl(url: string) {
    this.videoEmbedUrl = url && (url.includes('youtube.com') || url.includes('youtu.be'))
      ? `https://www.youtube.com/embed/${this.extractVideoId(url)}`
      : url || '';
    this.cdr.detectChanges();
  }

  loadLessonData() {
    if (!this.lessonId) return;

    this.loading = true;
    this.courseService.getLessonById(this.lessonId).subscribe({
      next: (result: any) => {
        if (result.status === 1) {
          this.lesson = result.data;
          this.lessonPages = this.lesson.lessonPages || [];
          this.lessonPages.sort((a: any, b: any) => a.position - b.position);
        }
        this.loading = false;
      },
      error: (error: any) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load lesson data'
        });
        this.loading = false;
      }
    });
  }

  openNewPageDialog() {
    this.editingPageId = null;

    this.uploadedFile = null;
    this.uploadedFileName = '';
    this.uploadedFileUrl = '';
    this.uploadedVideoFile = null;
    this.uploadedVideoName = '';
    this.uploadedVideoUrl = '';
    this.selectedVideoSource = 'URL';
    this.displayPageDialog = true;

    setTimeout(() => {
      this.pageForm.reset({
        qType: 'CONTENT',
        position: this.lessonPages.length
      });
    }, 0)
  }

  openEditPageDialog(page: any) {
    this.editingPageId = page.id;

    // Reset all uploaded file states
    this.uploadedFile = null;
    this.uploadedFileName = '';
    this.uploadedFileUrl = '';
    this.uploadedVideoFile = null;
    this.uploadedVideoName = '';
    this.uploadedVideoUrl = '';
    // Base form values
    const formValues: LessonPages = {
      title: page.title,
      qType: page.qType,
      position: page.position
    };



    // Handle content based on qType
    switch(page.qType) {
      case 'DOCUMENT':
        if (page.content) {
          this.uploadedFileUrl = page.content;
          this.uploadedFileName = page.content.split('/').pop() || 'Document';
        }
        formValues.content = page.content; // Set content as file path
        break;

      case 'VIDEO':
        if (page.content && page.content.startsWith('http')) {
          this.selectedVideoSource = 'URL';
          formValues.content = page.content; // Set content as URL
        } else if (page.content) {
          this.selectedVideoSource = 'UPLOAD';
          this.uploadedVideoUrl = page.content;
          this.uploadedVideoName = page.content.split('/').pop() || 'Video';
          formValues.content = ""
        }
        break;
      default:
        formValues.content = page.content; // Regular HTML content
        break;
    }

    // Apply values to form
    setTimeout(() => {
      this.pageForm.patchValue(formValues);
    }, 0)
    this.displayPageDialog = true;
  }

  savePage() {
    if (this.pageForm.invalid) {
      this.messageService.add({
        severity: 'error',
        summary: 'Validation Error',
        detail: 'Please complete all required fields'
      });
      return;
    }

    const formValues = this.pageForm.getRawValue(); // Use getRawValue() to include disabled controls

    // Create the page data object with explicit properties
    const pageData: LessonPages = {
      title: formValues.title,
      qType: formValues.qType, // Make sure this is explicitly included
      content: formValues.content,
      position: formValues.position,
      lessonId: Number(this.lessonId)
    };
    console.log('Sending page data:', pageData); // Debug output
    if (this.editingPageId) {
      // Update existing page
      // this.courseService.updateLessonPage(this.editingPageId, pageData).subscribe({
      //   next: (response) => {
      //     if (response.status === 1) {
      //       this.messageService.add({
      //         severity: 'success',
      //         summary: 'Success',
      //         detail: 'Lesson page updated successfully'
      //       });
      //       this.displayPageDialog = false;
      //       this.loadLessonData();
      //     }
      //   },
      //   error: (error) => {
      //     this.messageService.add({
      //       severity: 'error',
      //       summary: 'Error',
      //       detail: 'Failed to update lesson page'
      //     });
      //   }
      // });
    } else {
      // Create new page
      if(pageData.qType === 'CONTENT') {
        this.courseService.addContentLessonPage(pageData).subscribe({
          next: (response: any) => {
            if (response.status === 1) {
              this.messageService.add({
                severity: 'success',
                summary: 'Success',
                detail: 'Lesson page created successfully'
              });
              this.displayPageDialog = false;
              this.loadLessonData();
            }
          },
          error: (error: any) => {
            console.error('Error adding lesson page:', error);
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Failed to create lesson page'
            });
          }
        });
      }
      else if(pageData.qType === 'DOCUMENT') {
        if (this.uploadedFile) {
          this.courseService.addDocumentLessonPage(pageData, this.uploadedFile).subscribe({
            next: (response: any) => {
              if (response.status === 1) {
                this.messageService.add({
                  severity: 'success',
                  summary: 'Success',
                  detail: 'Lesson page created successfully'
                });
                this.displayPageDialog = false;
                this.loadLessonData();
              }
            },
            error: (error: any) => {
              this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: 'Failed to create lesson page'
              })
            }
          });
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Please select a file to upload'
          });
        }
      }
      else if(pageData.qType == "VIDEO"){
        if (this.selectedVideoSource === 'URL') {
          this.courseService.addVideoURLLessonPage(pageData).subscribe({
            next: (response: any) => {
              if (response.status === 1) {
                this.messageService.add({
                  severity: 'success',
                  summary: 'Success',
                  detail: 'Lesson page created successfully'
                });
                this.displayPageDialog = false;
                this.loadLessonData();
              }
            }
          })
        } else {
          if(this.uploadedVideoFile) {
            pageData.content = "";
            this.courseService.addUploadVideoLessonPage(pageData, this.uploadedVideoFile).subscribe({
              next: (response: any) => {
                if (response.status === 1) {
                  this.messageService.add({
                    severity: 'success',
                    summary: 'Success',
                    detail: 'Lesson page created successfully'
                  });
                  this.displayPageDialog = false;
                  this.loadLessonData();
                }
              }
            })
          } else {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Please select a file to upload'
            });
          }
        }
      }
    }
  }

  confirmDeletePage(page: any) {
    this.confirmationService.confirm({
      message: 'Are you sure you want to delete this page?',
      header: 'Delete Confirmation',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.deletePage(page.id);
      }
    });
  }

  deletePage(id: number) {
    this.courseService.deleteLessonPageById(id).subscribe({
      next: (response) => {
        if (response.status === 1) {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Lesson page deleted successfully'
          });
          this.loadLessonData();
        }
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to delete lesson page'
        });
      }
    });
  }

  onPageDrop(event: CdkDragDrop<any[]>) {
    if (event.previousIndex === event.currentIndex) return;

    moveItemInArray(this.lessonPages, event.previousIndex, event.currentIndex);

    // Update positions
    this.lessonPages.forEach((page, index) => {
      page.position = index;
      // Call API to update position
      // this.courseService.updateLessonPage(page.id, { position: index }).subscribe();
    });
  }

  previewLesson() {
    this.router.navigate(['/user/lesson-detail', this.lessonId]);
  }

  getSafeVideoUrl(url: string) {
    console.log('getSafeVideoUrl called with:', url);
    if (!url) return '';
    if(url.includes('youtube.com') || url.includes('youtu.be')){
      url = 'https://www.youtube.com/embed/' + this.extractVideoId(url);
      console.log('Converted URL:', url);
    }
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  }

  extractVideoId(url: string): string {
    const regExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|\&v=)([^#\&\?]*).*/;
    const match = url.match(regExp);
    return (match && match[2].length === 11) ? match[2] : '';
  }

  getTagSeverity(qType: string): any {
    switch(qType) {
      case 'CONTENT': return 'info';
      case 'VIDEO': return 'success';
      case 'PROGRAMMING': return 'warning';
      case 'SHORT_ANS': return 'primary';
      case 'DOCUMENT': return 'danger';
      default: return 'info';
    }
  }


  // Add these methods
  onFileSelect(event: any) {
    if (event.files && event.files.length > 0) {
      this.uploadedFile = event.files[0];
      this.uploadedFileName =  this.uploadedFile?.name || 'File';
    }
  }


  onFileUpload(event: any) {
    if (!this.uploadedFile) return;

    // In a real application, you would upload the file to your server
    // For now, we'll simulate a successful upload
    const formData = new FormData();
    formData.append('file', this.uploadedFile);

    // Call your service to upload the file
    // this.courseService.uploadLessonFile(formData).subscribe({
    //   next: (response) => {
    //     if (response.status === 1) {
    //       this.uploadedFileUrl = response.data.fileUrl;
    //       this.pageForm.patchValue({
    //         content: this.uploadedFileUrl
    //       });
    //       this.messageService.add({
    //         severity: 'success',
    //         summary: 'Success',
    //         detail: 'File uploaded successfully'
    //       });
    //     }
    //   },
    //   error: (error) => {
    //     this.messageService.add({
    //       severity: 'error',
    //       summary: 'Error',
    //       detail: 'Failed to upload file'
    //     });
    //   }
    // });

    // Simulation for now
    // setTimeout(() => {
    //   this.uploadedFileUrl = `uploads/${this.uploadedFileName}`;
    //   this.pageForm.patchValue({
    //     content: this.uploadedFileUrl
    //   });
    //   this.messageService.add({
    //     severity: 'success',
    //     summary: 'Success',
    //     detail: 'File uploaded successfully'
    //   });
    // }, 1000);
  }


  removeUploadedFile() {
    this.uploadedFile = null;
    this.uploadedFileName = '';
    this.uploadedFileUrl = '';
    this.pageForm.patchValue({
      content: ''
    });
  }

  onVideoFileSelect(event: any) {
    if (event.files && event.files.length > 0) {
      this.uploadedVideoFile = event.files[0];
      this.uploadedVideoName = this.uploadedVideoFile?.name || 'Video';
    }
  }


  onVideoFileUpload(event: any) {
    if (!this.uploadedVideoFile) return;

    // In a real application, you would upload the file to your server
    const formData = new FormData();
    formData.append('file', this.uploadedVideoFile);

    // Call your service to upload the file
    // this.courseService.uploadVideoFile(formData).subscribe({
    //   next: (response) => {
    //     if (response.status === 1) {
    //       this.uploadedVideoUrl = response.data.fileUrl;
    //       this.pageForm.patchValue({
    //         content: this.uploadedVideoUrl
    //       });
    //       this.messageService.add({
    //         severity: 'success',
    //         summary: 'Success',
    //         detail: 'Video uploaded successfully'
    //       });
    //     }
    //   },
    //   error: (error) => {
    //     this.messageService.add({
    //       severity: 'error',
    //       summary: 'Error',
    //       detail: 'Failed to upload video'
    //     });
    //   }
    // });

    // Simulation for now
    // setTimeout(() => {
    //   this.uploadedVideoUrl = URL.createObjectURL(this.uploadedVideoFile);
    //   this.pageForm.patchValue({
    //     content: `uploads/videos/${this.uploadedVideoName}`
    //   });
    //   this.messageService.add({
    //     severity: 'success',
    //     summary: 'Success',
    //     detail: 'Video uploaded successfully'
    //   });
    // }, 1000);
  }

  removeUploadedVideo() {
    this.uploadedVideoFile = null;
    this.uploadedVideoName = '';
    this.uploadedVideoUrl = '';
    this.pageForm.patchValue({
      content: ''
    });
  }


}
