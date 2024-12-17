import pygame
import sys
import os
import random
from typing import List, Tuple

# Ensure tracking folder exists
TRACKING_FOLDER = 'knight_tour_tracking'
os.makedirs(TRACKING_FOLDER, exist_ok=True)

class KnightTourVisualizer:
    def __init__(self, board_config: List[List[int]], cell_size: int = None):
        """
        Initialize the Knight Tour Visualizer with dynamic board sizing
        
        :param board_config: 2D list representing the board configuration
        :param cell_size: Optional custom cell size, otherwise auto-calculated
        """
        pygame.init()
        
        # Determine board size from configuration
        self.board_config = board_config
        self.BOARD_SIZE = len(board_config)
        
        # Dynamically calculate cell size to fit screen
        if cell_size is None:
            # Get screen resolution
            infoObject = pygame.display.Info()
            screen_width = infoObject.current_w
            screen_height = infoObject.current_h
            
            # Calculate max possible cell size while keeping entire board visible
            max_cell_width = (screen_width - 100) // self.BOARD_SIZE
            max_cell_height = (screen_height - 100) // self.BOARD_SIZE
            
            # Choose the smaller dimension to ensure full visibility
            self.CELL_SIZE = min(max_cell_width, max_cell_height, 150)
        else:
            self.CELL_SIZE = cell_size
        
        # Calculate screen size
        self.SCREEN_SIZE_X = self.BOARD_SIZE * self.CELL_SIZE
        self.SCREEN_SIZE_Y = self.BOARD_SIZE * self.CELL_SIZE
        
        # Pygame setup with dynamic sizing
        self.screen = pygame.display.set_mode((self.SCREEN_SIZE_X, self.SCREEN_SIZE_Y))
        pygame.display.set_caption("🏇 Magical Knight's Mystical Tour 🏇")
        
        # Enhanced color palette with gradients and creativity
        self.BACKGROUND_COLOR = (30, 30, 50)  # Deep midnight blue
        self.LIGHT_SQUARE = self.gradient_color((240, 217, 181), (220, 197, 161))
        self.DARK_SQUARE = self.gradient_color((181, 136, 99), (161, 116, 79))
        
        # Particle and effect colors
        self.PARTICLE_COLORS = [
            (255, 0, 0),    # Vibrant red
            (0, 255, 0),    # Bright green
            (0, 0, 255),    # Deep blue
            (255, 165, 0),  # Orange
            (255, 0, 255),  # Magenta
            (255, 255, 0),  # Yellow
            (128, 0, 128)   # Purple
        ]
        
        # Fonts
        pygame.font.init()
        self.font = pygame.font.Font(None, max(24, self.CELL_SIZE // 5))
        self.large_font = pygame.font.Font(None, max(48, self.CELL_SIZE // 2.5))
        
        # Tour path and particles
        self.tour_path = self.load_tour_path()
        self.particles = []
        
        # Particle and effect management
        self.max_particles = 100
        self.particle_timer = 0
        
        # Load enhanced knight image
        self.knight_img = self.create_knight_surface()
        
        # Pygame clock for smooth animation
        self.clock = pygame.time.Clock()
    
    def gradient_color(self, color1, color2):
        """Create a gradient between two colors"""
        return tuple(
            int((color1[i] + color2[i]) / 2) 
            for i in range(3)
        )
    
    def create_particle(self, x, y):
        """Create a magical particle with random properties"""
        return {
            'x': x,
            'y': y,
            'color': random.choice(self.PARTICLE_COLORS),
            'size': random.randint(2, max(3, self.CELL_SIZE // 20)),
            'speed_x': random.uniform(-2, 2),
            'speed_y': random.uniform(-2, 2),
            'life': random.randint(30, 60)
        }
    
    def update_particles(self):
        """Update and render magical particles"""
        for particle in self.particles[:]:
            particle['x'] += particle['speed_x']
            particle['y'] += particle['speed_y']
            particle['life'] -= 1
            
            # Render particle
            pygame.draw.circle(
                self.screen, 
                particle['color'], 
                (int(particle['x']), int(particle['y'])), 
                particle['size']
            )
            
            # Remove dead particles
            if particle['life'] <= 0:
                self.particles.remove(particle)
    
    def create_knight_surface(self) -> pygame.Surface:
        """Create a magical knight surface with glowing effect"""
        knight_surf = pygame.Surface((self.CELL_SIZE, self.CELL_SIZE), pygame.SRCALPHA)
        
        # Mystical knight shape
        points = [
            (self.CELL_SIZE * 0.3, self.CELL_SIZE * 0.7),
            (self.CELL_SIZE * 0.7, self.CELL_SIZE * 0.7),
            (self.CELL_SIZE * 0.5, self.CELL_SIZE * 0.3)
        ]
        
        # Draw knight with gradient and glow
        pygame.draw.polygon(knight_surf, (50, 50, 200), points)
        
        # Add a glowing effect
        glow_surf = pygame.Surface((self.CELL_SIZE, self.CELL_SIZE), pygame.SRCALPHA)
        for i in range(10, 0, -1):
            glow_color = (50, 50, 200, 30 - i)
            pygame.draw.polygon(glow_surf, glow_color, 
                [tuple(p * (1 + i * 0.05) for p in point) for point in points])
        
        knight_surf.blit(glow_surf, (0, 0))
        return knight_surf
    
    def load_tour_path(self) -> List[Tuple[int, int]]:
        """
        Load the knight's tour path from path.txt
        
        :return: List of (x, y) coordinates for the knight's tour
        """
        path = []
        with open('path.txt', 'r') as f:
            for line in f:
                if line.startswith('Step'):
                    coords_str = line.split('(')[1].split(')')[0]
                    x, y = map(int, coords_str.split(','))
                    path.append((x, y))
        return path
    
    def draw_board(self):
        """Draw the chess board with magical alternating colors"""
        for row in range(self.BOARD_SIZE):
            for col in range(self.BOARD_SIZE):
                rect = pygame.Rect(
                    col * self.CELL_SIZE, 
                    row * self.CELL_SIZE, 
                    self.CELL_SIZE, 
                    self.CELL_SIZE
                )
                
                # Magical color alternation
                color = self.LIGHT_SQUARE if (row + col) % 2 == 0 else self.DARK_SQUARE
                pygame.draw.rect(self.screen, color, rect)
                
                # Subtle coordinate marking
                coord_text = f"{chr(col + 97)}{self.BOARD_SIZE - row}"
                text_surf = self.font.render(coord_text, True, (100, 100, 100))
                text_rect = text_surf.get_rect(
                    center=(
                        col * self.CELL_SIZE + self.CELL_SIZE // 2, 
                        row * self.CELL_SIZE + self.CELL_SIZE // 2
                    )
                )
                self.screen.blit(text_surf, text_rect)
    
    def visualize_tour(self):
        """
        Visualize the entire knight's tour with magical step-by-step animation
        """
        for step, (x, y) in enumerate(self.tour_path):
            # Clear the screen with a magical background
            self.screen.fill(self.BACKGROUND_COLOR)
            
            # Draw the board
            self.draw_board()
            
            # Draw historical path
            for prev_step in range(step + 1):
                prev_x, prev_y = self.tour_path[prev_step]
                
                # Fade effect for historical moves
                opacity = int(255 * (prev_step / step)) if step > 0 else 255
                historical_knight = self.knight_img.copy()
                historical_knight.set_alpha(opacity)
                
                knight_rect = historical_knight.get_rect(
                    center=(
                        prev_x * self.CELL_SIZE + self.CELL_SIZE // 2, 
                        prev_y * self.CELL_SIZE + self.CELL_SIZE // 2
                    )
                )
                self.screen.blit(historical_knight, knight_rect)
                
                # Draw path lines with fading
                if prev_step > 0:
                    prev_prev_x, prev_prev_y = self.tour_path[prev_step - 1]
                    line_surface = pygame.Surface((self.SCREEN_SIZE_X, self.SCREEN_SIZE_Y), pygame.SRCALPHA)
                    pygame.draw.line(
                        line_surface, 
                        (*self.PARTICLE_COLORS[prev_step % len(self.PARTICLE_COLORS)], opacity),
                        (prev_prev_x * self.CELL_SIZE + self.CELL_SIZE // 2, 
                         prev_prev_y * self.CELL_SIZE + self.CELL_SIZE // 2),
                        (prev_x * self.CELL_SIZE + self.CELL_SIZE // 2, 
                         prev_y * self.CELL_SIZE + self.CELL_SIZE // 2),
                        5
                    )
                    self.screen.blit(line_surface, (0, 0))
            
            # Generate magical particles
            center_x = x * self.CELL_SIZE + self.CELL_SIZE // 2
            center_y = y * self.CELL_SIZE + self.CELL_SIZE // 2
            
            # Add new particles
            if len(self.particles) < self.max_particles:
                for _ in range(random.randint(10, 20)):
                    particle = self.create_particle(center_x, center_y)
                    self.particles.append(particle)
            
            # Update and draw particles
            self.update_particles()
            
            # Draw step number with magical styling
            step_text = self.large_font.render(f"Step {step + 1}", True, (255, 255, 255))
            self.screen.blit(step_text, (10, 10))
            
            # Update display
            pygame.display.flip()
            
            # Save screenshot
            screenshot_path = os.path.join(TRACKING_FOLDER, f'step_{step + 1:02d}.png')
            pygame.image.save(self.screen, screenshot_path)
            
            # Smooth animation
            self.clock.tick(20)  # 20 FPS for detailed observation
            
            # Event handling
            for event in pygame.event.get():
                if event.type == pygame.QUIT:
                    pygame.quit()
                    sys.exit()
        
        # Keep window open after tour is complete
        waiting = True
        while waiting:
            for event in pygame.event.get():
                if event.type == pygame.QUIT:
                    waiting = False
        
        pygame.quit()

def main():
    # Read the board configuration
    with open('board.txt', 'r') as f:
        board_config = [list(map(int, line.split())) for line in f.readlines()]
    
    # Initialize and run visualization
    visualizer = KnightTourVisualizer(board_config)
    visualizer.visualize_tour()

if __name__ == "__main__":
    main()