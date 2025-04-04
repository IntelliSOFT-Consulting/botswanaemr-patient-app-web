# Dockerfile for React client

# Build react client
FROM node:16-alpine 

# Working directory be app
WORKDIR /app

COPY package.json ./

###  Installing dependencies

RUN npm install --silent

# copy local files to app folder
COPY . .

FROM nginx:alpine
COPY docker/dev/nginx.conf /etc/nginx/conf.d/default.conf 
COPY --from=build-stage /app/build /usr/share/nginx/html 


CMD ["npm","start", "&&", "nginx", "-g", "daemon off;"]

