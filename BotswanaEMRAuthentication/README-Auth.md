On Server....
export ENV_PROFILE=server
sudo docker compose up -d --build

On Local...
set -a  
source .env.server
set +a

mvn spring-boot:run  