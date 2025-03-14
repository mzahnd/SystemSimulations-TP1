import argparse
from dataclasses import dataclass

import matplotlib.pyplot as plt
import matplotlib.animation as animation


@dataclass(eq=True)
class Particle:
  index: int
  radius: float
  interaction_radius: float
  x: float
  y: float


def parse_particles(lines, particle_radius, interaction_radius) -> [Particle]:
  particles = []
  for i, line in enumerate(lines):
    line = line.replace("\n", "")
    data = line.split(' ')
    particles.append(Particle(index=i, radius=float(particle_radius),
                              interaction_radius=float(interaction_radius),
                              x=float(data[0]),
                              y=float(data[1])))

  return particles


def parse_neighbors(lines):
  neighbours = {}
  for i, line in enumerate(lines):
    line = line.replace("\n", "")
    i_neighbours_str = line.split(' ')[1:]
    i_neighbours = []
    for neighbour in i_neighbours_str:
      if neighbour == '':
        continue
      i_neighbours.append(int(neighbour))
    neighbours[i] = i_neighbours

  return neighbours


def add_grid(matrix_size, ax, board_side_length):
  delta = board_side_length / matrix_size
  # Draw vertical lines at intervals of 1 unit, from 0 to M
  for x in range(0, matrix_size + 1):  # M + 1 to include the last line
    ax.vlines(x*delta, 0, board_side_length, colors='gray', linestyle='--', linewidth=0.2)
  # Draw horizontal lines at intervals of 1 unit, from 0 to M
  for y in range(0, matrix_size + 1):  # M + 1 to include the last line
    ax.hlines(y*delta, 0, board_side_length, colors='gray', linestyle='--', linewidth=0.2)


def animate(particles: [Particle], neighbours: dict, filename: str,
    matrix_size: int | None, board_side_length: int):
  fig, ax = plt.subplots(figsize=(8, 8))
  ax.set_xlim(0, 25)
  ax.set_ylim(0, 25)

  def update(frame):
    ax.clear()  # Clear the previous plot

    plt.title(
      f"Interacción de Partículas: Método {"Cell Index" if matrix_size else "Brute Force"}")
    plt.xlabel("x")
    plt.ylabel("y")
    # Set limits and labels again after clearing
    ax.set_xlim(-1, 21)
    ax.set_ylim(-1, 21)

    if matrix_size:
      add_grid(matrix_size, ax, board_side_length)

    # Get the particle to focus on (i-particle)
    focused_particle = particles[frame]
    focused_particle_pos = (focused_particle.x, focused_particle.y)

    # Plot the nearby particles with a softer color
    neighbours_scatter = []
    for neighbor_idx in neighbours[frame]:
      p = particles[neighbor_idx]
      neighbours_scatter.append(ax.scatter(p.x, p.y, color='lightblue', s=100))

    particle_interaction_radius = plt.Circle(focused_particle_pos,
                                             focused_particle.interaction_radius,
                                             color='green', fill=False,
                                             linestyle='dotted', linewidth=2)
    ax.add_patch(particle_interaction_radius)

    focused_circle = plt.Circle(focused_particle_pos, focused_particle.radius,
                                color='red', fill=True, linestyle='dashed',
                                linewidth=1)
    ax.add_patch(focused_circle)

    # Optionally, plot the particles' radii
    filtered_particles = list(
        filter(lambda x: x.index not in neighbours[frame] + [frame], particles))
    ax.text(focused_particle.x, focused_particle.y - 0.5, str(frame),
            color='black', fontsize=12, ha='center', va='bottom')
    non_neighbour_particles = []
    for p in filtered_particles:
      circle = plt.Circle((p.x, p.y), p.radius, color='orange', fill=True,
                          linestyle='dashed', linewidth=1)
      non_neighbour_particles.append(ax.add_patch(circle))
    return [focused_circle] + neighbours_scatter + [
      particle_interaction_radius] + non_neighbour_particles

  ani = animation.FuncAnimation(fig, update, frames=len(particles),
                                interval=0.15)

  writer = animation.FFMpegWriter(fps=2)

  ani.save(f'{filename}.mp4', writer=writer)


def main():
  parser = argparse.ArgumentParser(
      description="Parse Kotlin output file and generate animations and plots.")
  parser.add_argument("-m", "--matrix_size", type=int,
                      help="Number of matrix cells (MxM)")
  parser.add_argument("-d", "--input_directory", type=str,
                      help="Kotlin output file to parse")
  parser.add_argument("-n", "--number_of_particles", type=int,
                      help="Total number of particles")
  parser.add_argument("-l", "--board_side_length", type=int,
                      help="Side length of the board")
  parser.add_argument("-rc", "--interaction_radius", type=float,
                      help="Radius of interaction of the particle")
  parser.add_argument("-r", "--particle_radius", type=float,
                      help="Radius of the particle")
  parser.add_argument("-p", "--periodic_contour", action='store_true',
                      help="Is periodic contour enabled")
  parser.add_argument("-b", "--brute_force", action='store_true',
                      help="Analyse brute force output")

  args = parser.parse_args()

  matrix_size = args.matrix_size
  input_directory = args.input_directory
  number_of_particles = args.number_of_particles
  board_side_length = args.board_side_length
  interaction_radius = args.interaction_radius
  particle_radius = args.particle_radius
  periodic_contour = "true" if args.periodic_contour else "false"

  dynamic_config_file_path = "./dynamic_config.txt"

  input_file_name = None
  output_name = None
  if args.brute_force:
    input_file_name = f"BRUTE_FORCE-particles={number_of_particles}-M={matrix_size}-rc={interaction_radius}-r={particle_radius}-periodic={periodic_contour}-L={board_side_length}.txt"
    output_name = "BRUTE_FORCE"
  else:
    input_file_name = f"CELL_INDEX_METHOD-particles={number_of_particles}-M={matrix_size}-rc={interaction_radius}-r={particle_radius}-periodic={periodic_contour}-L={board_side_length}.txt"
    output_name = "CELL_INDEX"

  input_file_path = f"{input_directory}/{input_file_name}"

  try:
    with open(input_file_path, 'r') as input:
      with open(dynamic_config_file_path, "r") as dynamic_config:
        # Read each line from the file
        output_lines = input.readlines()
        dynamic_config_lines = dynamic_config.readlines()[1:]
        particles = parse_particles(dynamic_config_lines,
                                    particle_radius,
                                    interaction_radius)
        cell_index_neighbours = parse_neighbors(output_lines)
        animate(particles, cell_index_neighbours, output_name,
                matrix_size if not args.brute_force else None,
                board_side_length)
  except FileNotFoundError as e:
    print(e)
  except Exception as e:
    print(f"An error occurred: {e}")


if __name__ == "__main__":
  main()
