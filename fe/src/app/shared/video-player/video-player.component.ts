import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {SafeResourceUrl} from "@angular/platform-browser";

@Component({
  selector: 'app-video-player',
  templateUrl: './video-player.component.html',
  styleUrl: './video-player.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class VideoPlayerComponent {
  @Input() videoUrl: SafeResourceUrl | null = null; // Nhận URL từ component cha

}
