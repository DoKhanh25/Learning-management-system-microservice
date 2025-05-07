import {Component, OnInit} from '@angular/core';
import {CourseService} from "../../../services/course/course.service";
import {QuizService} from "../../../services/quiz/quiz.service";
import {ActivatedRoute} from "@angular/router";
import {TreeGridNode} from "../../../../model/treenode";
import {TreeNode} from "primeng/api";
import {forkJoin} from "rxjs";
import {MessageService} from 'primeng/api';

@Component({
  selector: 'app-course-statistic',
  templateUrl: './course-statistic.component.html',
  styleUrl: './course-statistic.component.css',
  providers: [MessageService]
})


export class CourseStatisticComponent implements OnInit{

  courseId: number = 0;
  treeNodes: TreeGridNode[] = [];
  hierarchicalTree: TreeNode[] = [];
  selectedNode: TreeGridNode | null = null;
  selectedTreeNode: TreeNode | null = null;
  selectedNodeData: any = null;
  loading: boolean = true;
  detailsLoading: boolean = false;

  // UI enhancement properties
  courseTitle: string = '';
  nodeCounts: {
    sections: number;
    lessons: number;
    pages: number;
    assignments: number;
    exams: number;
  } = {sections: 0, lessons: 0, pages: 0, assignments: 0, exams: 0};
  nodeColors: {[key: string]: string} = {
    courseSection: '#2196F3',
    lesson: '#4CAF50',
    lessonPage: '#FFC107',
    assignment: '#FF5722',
    exam: '#9C27B0'
  };

  constructor(
    private route: ActivatedRoute,
    private courseService: CourseService,
    private quizService: QuizService,
    private messageService: MessageService
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.courseId = +params['courseId'];
      if (this.courseId) {
        this.loadCourseInfo();
        this.loadTreeData();
      }
    });
  }

  loadCourseInfo(): void {
    this.courseService.getCourseById(this.courseId).subscribe({
      next: (result) => {
        if (result.status === 1 && result.data) {
          this.courseTitle = result.data.name;
        }
      },
      error: (error) => {
        console.error('Error loading course info:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load course information'
        });
      }
    });
  }

  loadTreeData(): void {
    this.loading = true;

    // Use forkJoin to make parallel requests
    forkJoin([
      this.courseService.getLessonPagesTree(this.courseId),
      this.courseService.getAssignmentsTree(this.courseId),
      this.quizService.getExamsTree(this.courseId)
    ]).subscribe({
      next: ([lessonPagesResult, assignmentsResult, examsResult]) => {
        this.treeNodes = [];

        // Add lesson pages to tree
        if (lessonPagesResult.status === 1 && lessonPagesResult.data) {
          this.treeNodes = [...this.treeNodes, ...lessonPagesResult.data];
        }

        // Add assignments to tree
        if (assignmentsResult.status === 1 && assignmentsResult.data) {
          this.treeNodes = [...this.treeNodes, ...assignmentsResult.data];
        }

        // Add exams to tree
        if (examsResult.status === 1 && examsResult.data) {
          this.treeNodes = [...this.treeNodes, ...examsResult.data];
        }

        // Transform flat structure to hierarchical structure for PrimeNG Tree
        this.transformToTreeNodes();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading tree data:', error);
        this.loading = false;
      }
    });
  }

  transformToTreeNodes(): void {
    this.nodeCounts = {sections: 0, lessons: 0, pages: 0, assignments: 0, exams: 0};
    const nodeMap = new Map<string, TreeNode>();

    // Group by type for top-level categories with badges
    const categories: TreeNode[] = [
      {
        key: 'content',
        label: 'Nội dung học tập',
        icon: 'pi pi-folder-open',
        children: [],
        expanded: true,
        styleClass: 'content-category',
        data: { type: 'category', color: '#2196F3' }
      },
      {
        key: 'assignments',
        label: 'Bài tập',
        icon: 'pi pi-pencil',
        children: [],
        expanded: true,
        styleClass: 'assignment-category',
        data: { type: 'category', color: '#FF5722' }
      },
      {
        key: 'exams',
        label: 'Bài kiểm tra',
        icon: 'pi pi-check-square',
        children: [],
        expanded: true,
        styleClass: 'exam-category',
        data: { type: 'category', color: '#9C27B0' }
      }
    ];

    // First, create all nodes and store in a map with enhanced styling
    this.treeNodes.forEach(node => {
      // Count nodes by type
      switch(node.type) {
        case 'courseSection': this.nodeCounts.sections++; break;
        case 'lesson': this.nodeCounts.lessons++; break;
        case 'lessonPage': this.nodeCounts.pages++; break;
        case 'assignment': this.nodeCounts.assignments++; break;
        case 'exam': this.nodeCounts.exams++; break;
      }

      const treeNode: TreeNode = {
        key: node.id.toString() + "-" + node.type,
        label: node.title,
        data: {
          ...node,
          color: this.nodeColors[node.type] || '#607D8B'
        },
        icon: this.getNodeIcon(node.type),
        children: [],
        expanded: node.type === 'courseSection',
        styleClass: `node-${node.type}`
      };

      let nodeId = node.id + "-" + node.type
      nodeMap.set(nodeId, treeNode);
    });

    // Then, establish parent-child relationships based on parentId and type hierarchy
    this.treeNodes.forEach(node => {
      let nodeId = node.id + "-" + node.type
      const treeNode = nodeMap.get(nodeId);
      if (!treeNode) return;

      if (node.type === 'assignment') {
        // Assignments go directly to the assignments category
        categories[1].children!.push(treeNode);
      } else if (node.type === 'exam') {
        // Exams go directly to the exams category
        categories[2].children!.push(treeNode);
      } else if (node.type === 'courseSection') {
        // Course sections go directly to the content category
        categories[0].children!.push(treeNode);
      } else if (node.type === 'lesson' && node.parentId) {
        // Lessons are children of course sections
        let nodeParentId = node.parentId + "-" + "courseSection"
        const parentNode = nodeMap.get(nodeParentId);
        if (parentNode) {
          parentNode.children!.push(treeNode);
        }
      } else if (node.type === 'lessonPage' && node.parentId) {
        // Lesson pages are children of lessons
        let nodeParentId = node.parentId + "-" + "lesson"
        const parentNode = nodeMap.get(nodeParentId);
        if (parentNode) {
          parentNode.children!.push(treeNode);
        }
      }
    });

    // Update category labels with counts
    categories[0].label = `Theo dõi tiến trình học tập (${this.nodeCounts.sections} chương mục, ${this.nodeCounts.lessons} bài học)`;
    categories[1].label = `Bài tập (${this.nodeCounts.assignments})`;
    categories[2].label = `Bài kiểm tra (${this.nodeCounts.exams})`;

    // Filter out empty categories
    this.hierarchicalTree = categories.filter(category => category.children!.length > 0);
  }

  getNodeIcon(type: string): string {
    switch (type) {
      case 'courseSection':
        return 'pi pi-folder';
      case 'lesson':
        return 'pi pi-book';
      case 'lessonPage':
        return 'pi pi-file';
      case 'assignment':
        return 'pi pi-pencil';
      case 'exam':
        return 'pi pi-check-square';
      default:
        return 'pi pi-file';
    }
  }

  onNodeSelect(event: any): void {
    if (event.node && event.node.data) {
      this.selectedNode = event.node.data;
      this.selectedTreeNode = event.node;
      this.detailsLoading = true;
      this.loadNodeDetails(this.selectedNode);
    }
  }

  onNodeExpand(event: any): void {
    const node = event.node;

    // Only load lesson pages if this is a lesson node and it doesn't already have children
    if (node.data?.type === 'lesson' && (!node.children || node.children.length === 0)) {
      // Add loading indicator to the node
      node.styleClass = 'loading-node';

      this.courseService.getLessonPagesByLessonId(node.data.id).subscribe({
        next: (result) => {
          if (result.status === 1 && result.data) {
            // Create new nodes for lesson pages with animations
            const lessonPageNodes: TreeNode[] = result.data.map((page: TreeGridNode) => ({
              key: page.id.toString(),
              label: page.title,
              data: {
                ...page,
                color: this.nodeColors['lessonPage'] || '#FFC107'
              },
              icon: 'pi pi-file',
              children: [],
              styleClass: 'animated-node node-lessonPage',
              tooltip: 'Lesson Page'
            }));

            // Update counts
            this.nodeCounts.pages += lessonPageNodes.length;

            // Add these nodes as children of the expanded lesson node
            node.children = lessonPageNodes;
            node.styleClass = 'expanded-node node-lesson';

            // Show success message for better UX
            this.messageService.add({
              severity: 'success',
              summary: 'Loaded',
              detail: `Loaded ${lessonPageNodes.length} lesson pages`,
              life: 2000
            });
          } else {
            node.styleClass = 'empty-node node-lesson';
          }
        },
        error: (error) => {
          console.error('Error loading lesson pages:', error);
          node.styleClass = 'error-node node-lesson';

          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to load lesson pages',
            life: 3000
          });
        }
      });
    }
  }

  // Format node type for display
  formatNodeType(type: any): string {
    switch (type) {
      case 'courseSection': return 'Course Section';
      case 'lesson': return 'Lesson';
      case 'lessonPage': return 'Lesson Page';
      case 'assignment': return 'Assignment';
      case 'exam': return 'Exam';
      default: return type;
    }
  }

  // Format date for display
  formatDate(date: string | Date): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  // Get styled badge for node type
  getNodeTypeBadge(type: string): string {
    const color = this.nodeColors[type] || '#607D8B';
    return `<span class="node-badge" style="background-color: ${color}">${this.formatNodeType(type)}</span>`;
  }

  loadNodeDetails(node: TreeGridNode | any): void {
    switch (node.type) {
      case 'lesson':
      case 'courseSection':
        this.loadLessonDetails(node);
        break;
      case 'assignment':
        this.loadAssignmentDetails(node);
        break;
      case 'exam':
        this.loadExamDetails(node);
        break;
      default:
        this.selectedNodeData = null;
        this.detailsLoading = false;
    }
  }

  loadLessonDetails(node: TreeGridNode): void {
    if (node.type === 'lesson') {
      // Load lesson details
      this.courseService.getStudentProgressByLessonId(node.id).subscribe({
        next: (result) => {
          if (result.status === 1) {
            let userIds: any[] = [];
            if(result.data != null && result.data.length > 0){
              result.data.forEach((e: any) => {
                userIds.push(e.userId);
              })
            }
            console.log(userIds);
            this.selectedNodeData = result.data;

          } else {
            this.selectedNodeData = null;
          }
          this.detailsLoading = false;
        },
        error: (error) => {
          console.error('Error loading lesson details:', error);
          this.selectedNodeData = null;
          this.detailsLoading = false;
        }
      });
    } else {
      // Load course section details
      this.courseService.findLessonEntitiesBySectionId(node.id).subscribe({
        next: (result) => {
          if (result.status === 1) {
            this.selectedNodeData = result.data;
          } else {
            this.selectedNodeData = null;
          }
          this.detailsLoading = false;
        },
        error: (error) => {
          console.error('Error loading section details:', error);
          this.selectedNodeData = null;
          this.detailsLoading = false;
        }
      });
    }
  }

  loadAssignmentDetails(node: TreeGridNode): void {
    this.courseService.getAssignmentById(node.id).subscribe({
      next: (result) => {
        if (result.status === 1) {
          this.selectedNodeData = result.data;

          // Get assignment submissions
          this.courseService.getAssignmentSubmissions(node.id).subscribe({
            next: (submissionsResult) => {
              if (submissionsResult.status === 1) {
                this.selectedNodeData.submissions = submissionsResult.data;
              }
              this.detailsLoading = false;
            },
            error: (error) => {
              console.error('Error loading assignment submissions:', error);
              this.detailsLoading = false;
            }
          });
        } else {
          this.selectedNodeData = null;
          this.detailsLoading = false;
        }
      },
      error: (error) => {
        console.error('Error loading assignment details:', error);
        this.selectedNodeData = null;
        this.detailsLoading = false;
      }
    });
  }

  loadExamDetails(node: TreeGridNode): void {
    this.quizService.getExamById(node.id).subscribe({
      next: (result) => {
        if (result.status === 1) {
          this.selectedNodeData = result.data;

          // Get exam submissions
          this.quizService.getExamSubmissions(node.id).subscribe({
            next: (submissionsResult) => {
              if (submissionsResult.status === 1) {
                this.selectedNodeData.submissions = submissionsResult.data;
              }
              this.detailsLoading = false;
            },
            error: (error) => {
              console.error('Error loading exam submissions:', error);
              this.detailsLoading = false;
            }
          });
        } else {
          this.selectedNodeData = null;
          this.detailsLoading = false;
        }
      },
      error: (error) => {
        console.error('Error loading exam details:', error);
        this.selectedNodeData = null;
        this.detailsLoading = false;
      }
    });
  }

}
