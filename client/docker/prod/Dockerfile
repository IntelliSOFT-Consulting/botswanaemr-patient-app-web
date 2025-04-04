FROM node:16-alpine  as build-stage 
WORKDIR /app 
COPY package.json ./
RUN npm install  --silent
COPY . . 
RUN npm run build


FROM nginx:alpine
COPY docker/prod/nginx.conf /etc/nginx/conf.d/default.conf 
COPY --from=build-stage /app/build /usr/share/nginx/html 

CMD ["nginx", "-g", "daemon off;"]
