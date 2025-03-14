output_directory=./example
board_side_length=20
interaction_radius=1
particle_radius=0.25
periodic_contour=false

clear
for ((j=20; j<=500; j=j+20)); do
  for ((i=1; i<=20; i++)); do
      # Add your custom commands here
      # Example: Check if the number is even or odd
      gradle run --no-build-cache --rerun-tasks \
                 --args="--matrix-size=${i} \
                         --output-directory=${output_directory} \
                         --number-of-particles=${j} \
                         --board-side-length=${board_side_length} \
                         --interaction-radius=${interaction_radius} \
                         --particle-radius=${particle_radius} \
                         --periodic-contour=${periodic_contour} \
                         --generate-random" 2>> output_loop.txt
    done
done